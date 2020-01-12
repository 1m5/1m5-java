/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.monetary.btc.packet;

/**
 * The sendcmpct message is defined as a message containing a 1-byte integer followed by a
 * 8-byte integer where pchCommand == "sendcmpct".
 *
 * The first integer SHALL be interpreted as a boolean (and MUST have a value of either 1 or 0)
 *
 * The second integer SHALL be interpreted as a little-endian version number.
 * Nodes sending a sendcmpct message MUST currently set this value to 1.
 *
 * Upon receipt of a "sendcmpct" message with the first and second integers set to 1,
 * the node SHOULD announce new blocks by sending a cmpctblock message.
 *
 * Upon receipt of a "sendcmpct" message with the first integer set to 0,
 * the node SHOULD NOT announce new blocks by sending a cmpctblock message,
 * but SHOULD announce new blocks by sending invs or headers, as defined by BIP130.
 *
 * Upon receipt of a "sendcmpct" message with the second integer set to something other than 1,
 * nodes MUST treat the peer as if they had not received the message (as it indicates the
 * peer will provide an unexpected encoding in cmpctblock, and/or other, messages).
 * This allows future versions to send duplicate sendcmpct messages with different versions
 * as a part of a version handshake for future versions.
 *
 * Nodes SHOULD check for a protocol version of >= 70014 before sending sendcmpct messages.
 *
 * Nodes MUST NOT send a request for a MSG_CMPCT_BLOCK object to a peer before having
 * received a sendcmpct message from that peer.
 *
 * This packet is only supported by protocol version >= 70014
 *
 * @author objectorange
 */
public class SendCmpctPacket extends BitcoinPacket {
}
