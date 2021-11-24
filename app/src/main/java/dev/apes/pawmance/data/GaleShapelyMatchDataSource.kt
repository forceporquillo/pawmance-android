@file:Suppress("unused")

package dev.apes.pawmance.data

import android.content.Context
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.apes.pawmance.data.auth.PetCollection
import dev.apes.pawmance.data.auth.PetMetadata
import dev.apes.pawmance.data.logs.LogsDao
import dev.apes.pawmance.di.ApplicationScope
import dev.apes.pawmance.ui.match.Pet
import dev.apes.pawmance.utils.getMaxPreferredDistance
import dev.apes.pawmance.utils.getMyCurrentLocation
import dev.apes.pawmance.utils.petCollection
import dev.apes.pawmance.utils.tryOffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.IllegalStateException
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

interface GaleShapelyMatchDataSource {
  fun getMyPossibleMatches(id: String): Flow<List<Pet>>
}

class PetMatchDataSource @Inject constructor(
  private val galeShapelyAlgorithm: GaleShapelyAlgorithm,
  private val firestore: FirebaseFirestore,
  private val logsDao: LogsDao
) : GaleShapelyMatchDataSource {

  private val executor = Executors.newSingleThreadExecutor()

  override fun getMyPossibleMatches(id: String): Flow<List<Pet>> {
    executor.execute(logsDao::clearLogs)

    Timber.i("Getting possible matches...")
    return callbackFlow<List<Pet>> {
      val listenerRegistration = firestore.petCollection()
        .addSnapshotListener { value, error ->
          if (value?.isEmpty == false) {
            val list = mutableListOf<Pair<String, PetCollection>>()
            var myCollection: PetCollection? = null
            Timber.i("Traversing pet collection metadata.")
            for (snapshot in value) {
              val collection = snapshot.toObject<PetCollection>()
              if (id == snapshot.id) {
                myCollection = collection
                Timber.i("Your metadata collection is found. Continue traversing...")
                continue
              } else {
                Timber.i("${collection.name} is added to queued matching list.")
                list.add(Pair(snapshot.id, collection))
              }
            }
            possibleMatchResults(Pair(id, myCollection), list)
          } else {
            tryOffer(emptyList())
            Timber.e(error)
          }
        }
      awaitClose { listenerRegistration.remove() }
    }
      .distinctUntilChanged()
  }

  private fun ProducerScope<List<Pet>>.possibleMatchResults(
    pair: Pair<String, PetCollection?>,
    list: MutableList<Pair<String, PetCollection>>
  ) {
    launch {
      galeShapelyAlgorithm
        .filterPossibleMatches(pair, list)
        .distinctUntilChanged()
        .collect(::tryOffer)
    }
  }

  interface GaleShapelyAlgorithm {
    fun filterPossibleMatches(
      myPetMetadata: Pair<String, PetCollection?>,
      theirPetaMetadata: List<Pair<String, PetCollection>>
    ): Flow<List<Pet>>
  }

  class GaleShapelyAlgorithmImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val externalScope: CoroutineScope
  ) : GaleShapelyAlgorithm {

    init {
      Timber.i("Initializing GaleShapelyAlgorithm...")
    }

    private fun hasNoPartnerWithOthers(
      pet: PetCollection,
      myId: String
    ): Boolean {
      return pet.partnerId == null || pet.partnerId == myId
    }

    private fun isOppositeGender(
      theirPetCollection: PetCollection,
      ourPetCollection: PetCollection?
    ): Boolean {
      if (ourPetCollection?.gender.isNullOrEmpty() || theirPetCollection.gender.isNullOrEmpty()) {
        throw IllegalStateException("Gender must not be null. But was your Gender: ${ourPetCollection?.gender}, their Gender: ${theirPetCollection.gender}")
      }
      return theirPetCollection.gender != ourPetCollection?.gender
    }

    override fun filterPossibleMatches(
      myPetMetadata: Pair<String, PetCollection?>,
      theirPetaMetadata: List<Pair<String, PetCollection>>
    ): Flow<List<Pet>> {
      Timber.i("Filtering possible matches.")
      return flow {
        val possibleMatches = currentLocation()
          .map { location ->
            Timber.i("Requesting current location.")
            val from =   if (location == null) {
              val myLocation = myPetMetadata.second?.location?.coordinates
              Timber.i("Retrieving server cache location Latitude: ${myLocation?.lat} Longitude: ${myLocation?.lng}")
              LatLng(myLocation?.lat ?: 0.0, myLocation?.lng ?: 0.0)
            } else {
              Timber.i("Retrieving local cache location Latitude: ${location.longitude} Longitude: ${location.longitude}")
              LatLng(location.latitude, location.longitude)
            }
            val haversineAlgorithm = HaversineAlgorithm(context, from = from)
            filterPossibleMatchesInternal(
              haversineAlgorithm,
              myPetMetadata,
              theirPetaMetadata
            )
          }
        emitAll(possibleMatches)
      }.shareIn(externalScope, SharingStarted.WhileSubscribed(), 1)
    }

    private fun filterPossibleMatchesInternal(
      haversineAlgorithm: HaversineAlgorithm,
      myPetMetadata: Pair<String, PetCollection?>,
      theirPetaMetadata: List<Pair<String, PetCollection>>
    ): List<Pet> {

      val unstable = mutableListOf<Pair<String, PetCollection>>()

      val myPetId = myPetMetadata.first
      val myPetCollection = myPetMetadata.second

      for (metadata in theirPetaMetadata) {
        val theirCollection = metadata.second

        Timber.i("${theirCollection.name ?: "Undefined"} breed is ${theirCollection.breed}.")

        if (!isBreedTheSame(myPetCollection, theirCollection)) {
          Timber.i("[Genetic Issue] Skipping ${theirCollection.name ?: "Undefined"} because of mismatch in breed.")
          continue
        }

        if (/* hasNoPartnerWithOthers(theirCollection, myPetId)
          && */ isOppositeGender(theirCollection, myPetCollection)
        ) {
          Timber.i("${metadata.second.name} is unstable. Adding to unstable list.")
          unstable.add(metadata)
        } else {
          Timber.i("${theirCollection.name} is discarded in fix matching, due to either same gender or has a partner already.")
        }

        // since we don't have preferences to depend on,
        // we use their pet preferences instead.
        theirCollection.preferences?.let { preferences ->
          // if at least contains one
          for (prefs in preferences) {
            if (myPreferences(myPetCollection)?.contains(prefs) == true) {
              if (!unstable.contains(metadata) && isOppositeGender(theirCollection, myPetCollection)) {
                unstable.add(metadata)
              }
              break
            }
          }
        }
      }

      Timber.i("---------------------------------------------")
      Timber.i("Stable Marriage preferred matches:")
      unstable.forEach {
        Timber.i("PetName: ${it.second.name} Gender: ${it.second.gender}")
      }
      Timber.i("---------------------------------------------")
      // lastly, we calculate their distance and sort it by KMS ascending.
      return haversineAlgorithm.calculateDistance(unstable.distinct(), myPetId)
    }

    private suspend fun currentLocation(): Flow<Location?> {
      return context.getMyCurrentLocation()
    }

    private fun myPreferences(second: PetCollection?) = second?.preferences

    private fun isBreedTheSame(
      myPetCollection: PetCollection?,
      theirCollection: PetCollection
    ) = myPetCollection?.breed == theirCollection.breed
  }

  data class DistanceComparator(
    val distance: Float,
    val metadata: PetMetadata
  )

  class HaversineAlgorithm(
    private val context: Context,
    private val from: LatLng
  ) {

    companion object {
      /**
       * The earth's radius, in meters.
       * Mean radius as defined by IUGG.
       */
      private const val EARTH_RADIUS = 6371009.0

      private const val KM_PER_METERS = 1000
    }

    init {
      Timber.i("Initializing Haversine Algorithm...")
    }

    @Synchronized
    fun computeDifference(
      distanceInMeters: Double,
      unsortedComparator: MutableList<DistanceComparator>,
      theirMetadata: Pair<String, PetCollection>
    ): List<DistanceComparator> {
      Timber.i("Calculating difference:")

      val calculatedDistance = formatDistance(distanceInMeters / 1000)
      Timber.i("Formatting distance to KM: $calculatedDistance, M: $distanceInMeters")

      val (withinKmBounds, maxDistance) = isWithInKmBounds(calculatedDistance)
      if (withinKmBounds) {
        val theirId = theirMetadata.first
        val collection = theirMetadata.second
        val metadata = PetMetadata(theirId, collection)

        Timber.i("${collection.name} is within the bounds of MAX search range: $maxDistance")
        Timber.i("Adding to unsorted distance comparator.")
        unsortedComparator.add(DistanceComparator(calculatedDistance, metadata))
      }

      Timber.i("List of unsorted collection within the bounds $unsortedComparator.")
      return unsortedComparator
    }

    fun calculateDistance(
      matchCoordinates: List<Pair<String, PetCollection>>,
      myId: String
    ): List<Pet> {
      val sortedMatches = mutableListOf<Pet>()

      val unsortedComparator = mutableListOf<DistanceComparator>()

      Timber.i("Calculating distance:")
      for (matchCoordinate in matchCoordinates) {
        val petCollection = matchCoordinate.second
        val coordinates = petCoordinates(petCollection)
        val distanceInMeters = computeDistanceBetween(coordinates)

        Timber.i("${petCollection.name} distance is $distanceInMeters meters.")
        computeDifference(
          distanceInMeters,
          unsortedComparator,
          matchCoordinate
        )
      }

      Timber.i("Sorting distance:")
      sortedMatches.addAll(
        unsortedComparator
          .sortedBy {
            Timber.i("PetName: ${it.metadata.collection.name} Distance: ${it.distance} KM/s.")
            it.distance
          }
          .map {
            val metadata = it.metadata
            val collection = metadata.collection
            val distance = it.distance
            Pet(
              id = metadata.petId,
              name = collection.name,
              distance = distance,
              breed = collection.breed,
              profile = collection.profile,
              isMatch = collection.partnerId == myId, // match to you
              tokenId = collection.tokenId
            )
          }
      )
      Timber.i("---------------------------------------------")
      Timber.i("Sorted Matches by distance ascending")
      sortedMatches.forEach {
        Timber.i("PetName: ${it.name} Distance: ${it.distance} KM/s.")
      }
      Timber.i("---------------------------------------------")
      return sortedMatches
    }

    private fun isWithInKmBounds(calculatedDistance: Float): Pair<Boolean, Int> {
      val maxDistanceSearch = context.getMaxPreferredDistance()
      return Pair(calculatedDistance < maxDistanceSearch, maxDistanceSearch)
    }

    private fun petCoordinates(collection: PetCollection): LatLng {
      val coordinates = collection.location?.coordinates
      return LatLng(coordinates?.lat ?: 0.0, coordinates?.lng ?: 0.0)
    }

    // Given h==hav(x), returns sin(abs(x)).
    fun sinFromHav(h: Double): Double {
      return 2 * sqrt(h * (1 - h))
    }

    // Returns hav(asin(x)).
    fun havFromSin(x: Double): Double {
      val x2 = x * x
      return x2 / (1 + sqrt(1 - x2)) * .5
    }

    // Returns sin(arcHav(x) + arcHav(y)).
    fun sinSumFromHav(
      x: Double,
      y: Double
    ): Double {
      val a = sqrt(x * (1 - x))
      val b = sqrt(y * (1 - y))
      return 2 * (a + b - 2 * (a * y + b * x))
    }

    /**
     * Returns hav() of distance from (lat1, lng1) to (lat2, lng2) on the unit sphere.
     */
    private fun havDistance(
      lat1: Double,
      lat2: Double,
      dLng: Double
    ): Double {
      return hav(lat1 - lat2) + hav(dLng) * cos(lat1) * cos(lat2)
    }

    /**
     * Returns the angle between two LatLngs, in radians. This is the same as the distance
     * on the unit sphere.
     */
    private fun computeAngleBetween(
      from: LatLng,
      to: LatLng
    ): Double {
      return distanceRadians(
        Math.toRadians(from.latitude), Math.toRadians(from.longitude),
        Math.toRadians(to.latitude), Math.toRadians(to.longitude)
      )
    }

    /**
     * Returns the distance between two LatLngs, in meters.
     */
    fun computeDistanceBetween(to: LatLng): Double {
      return computeAngleBetween(from, to) * EARTH_RADIUS
    }

    private fun distanceRadians(
      lat1: Double,
      lng1: Double,
      lat2: Double,
      lng2: Double
    ): Double {
      return arcHav(havDistance(lat1, lat2, lng1 - lng2))
    }

    fun clamp(
      x: Double,
      low: Double,
      high: Double
    ): Double {
      return if (x < low) low else if (x > high) high else x
    }

    /**
     * Wraps the given value into the inclusive-exclusive interval between min and max.
     *
     * @param n   The value to wrap.
     * @param min The minimum.
     * @param max The maximum.
     */
    fun wrap(
      n: Double,
      min: Double,
      max: Double
    ): Double {
      return if (n >= min && n < max) n else mod(n - min, max - min) + min
    }

    /**
     * Returns the non-negative remainder of x / m.
     *
     * @param x The operand.
     * @param m The modulus.
     */
    fun mod(
      x: Double,
      m: Double
    ): Double {
      return (x % m + m) % m
    }

    /**
     * Returns mercator Y corresponding to latitude.
     * See http://en.wikipedia.org/wiki/Mercator_projection .
     */
    fun mercator(lat: Double): Double {
      return ln(tan(lat * 0.5 + Math.PI / 4))
    }

    /**
     * Returns latitude from mercator Y.
     */
    fun inverseMercator(y: Double): Double {
      return 2 * atan(exp(y)) - Math.PI / 2
    }

    /**
     * Returns haversine(angle-in-radians).
     * hav(x) == (1 - cos(x)) / 2 == sin(x / 2)^2.
     */
    private fun hav(x: Double): Double {
      val sinHalf = sin(x * 0.5)
      return sinHalf * sinHalf
    }

    /**
     * Computes inverse haversine. Has good numerical stability around 0.
     * arcHav(x) == acos(1 - 2 * x) == 2 * asin(sqrt(x)).
     * The argument must be in [0, 1], and the result is positive.
     */
    private fun arcHav(x: Double): Double {
      return 2 * asin(sqrt(x))
    }

    private fun formatDistance(kms: Double): Float {
      return String.format(Locale.getDefault(), "%.1f", kms).toFloat()
    }
  }
}