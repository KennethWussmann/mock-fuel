package net.wussmann.kenneth.mockfuel.data

/**
 * Special type to not mock certain requests and let them pass through to their original destination and
 * return actual response from remote service
 */
object PassThroughResponse : AbstractResponse()
