package dev.forcecodes.pawmance.utils

import java.util.Stack

private const val MAX_SIZE = 4

class FixedByteArrayStack : Stack<ByteArray>() {
  override fun push(item: ByteArray?): ByteArray {
    while (size >= MAX_SIZE) {
      removeAt(0)
    }
    return super.push(item)
  }
}
