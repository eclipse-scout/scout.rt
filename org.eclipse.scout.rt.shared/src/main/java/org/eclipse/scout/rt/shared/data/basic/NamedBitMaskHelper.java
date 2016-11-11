package org.eclipse.scout.rt.shared.data.basic;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * <h3>{@link NamedBitMaskHelper}</h3> Helper class to access the bits of a bit-mask by names.<br>
 * The names are mapped to a bit position as needed. This means all uses of this class in an application share the bit
 * mapping positions.<br>
 * Currently there are {@value #NUM_BITS} bits supported.<br>
 *
 * @since 6.1
 */
public final class NamedBitMaskHelper {

  /**
   * Number of bit positions supported.
   */
  public static final byte NUM_BITS = 8;

  /**
   * Holder value with all bits set (all set to <code>true</code>).
   */
  public static final byte ALL_BITS_SET = (byte) 0xff;

  /**
   * Holder value with no bits set (all set to <code>false</code>).
   */
  public static final byte NO_BITS_SET = 0x0;

  private final Map<String /*bit-name*/, Byte /*bit-mask to access that position*/> m_masksByBitName;
  private byte m_nextIndex; // holds the next bit position to use

  /**
   * Creates a new instance of this helper with the given bit names already consumed in the order provided.
   * 
   * @param usedBitNames
   *          The bit names to consume eagerly.
   */
  public NamedBitMaskHelper(String... usedBitNames) {
    m_masksByBitName = new ConcurrentHashMap<>(NUM_BITS);
    m_nextIndex = 0;

    if (usedBitNames != null && usedBitNames.length > 0) {
      for (String usedName : usedBitNames) {
        bitMaskFor(usedName);
      }
    }
  }

  /**
   * Creates a new instance of this helper with no used bit names.
   */
  public NamedBitMaskHelper() {
    this((String[]) null);
  }

  /**
   * Changes the bit position associated with the given name to the given value.<br>
   * <br>
   * <b>Example:</b><code>
   * <pre>
   *  byte holder = 0;
   *  holder = NamedBitMaskHelper.changeBit("myName", true, holder);
   * </pre>
   * </code>
   *
   * @param bitName
   *          The name of the bit. Must not be <code>null</code>.
   * @param newBitValue
   *          <code>true</code> if the bit should be set, <code>false</code> if the bit should be cleared.
   * @param holder
   *          The holder of the bits.
   * @return The new value with the given bit value changed.
   * @throws AssertionException
   *           if the given bitName is <code>null</code>.
   * @throws IllegalStateException
   *           if too many distinct bitNames are used in the application. Currently {@value #NUM_BITS} different
   *           bitNames are supported.
   * @see #setBit(String, byte)
   * @see #clearBit(String, byte)
   * @see #NUM_BITS
   */
  public byte changeBit(String bitName, boolean newBitValue, byte holder) {
    byte mask = bitMaskFor(bitName);
    if (newBitValue) {
      return (byte) (holder | mask);
    }
    return (byte) (holder & (~mask));
  }

  /**
   * Sets the bit position associated with the given name to <code>true</code>.<br>
   * <br>
   * <b>Example:</b><code>
   * <pre>
   *  byte holder = 0;
   *  holder = NamedBitMaskHelper.setBit("myName", holder);
   * </pre>
   * </code>
   *
   * @param bitName
   *          The name of the bit. Must not be <code>null</code>.
   * @param holder
   *          The holder of the bits.
   * @return The new value with the given bit value set.
   * @throws AssertionException
   *           if the given bitName is <code>null</code>.
   * @throws IllegalStateException
   *           if too many distinct bitNames are used in the application. Currently {@value #NUM_BITS} different
   *           bitNames are supported.
   * @see #changeBit(String, boolean, byte)
   * @see #clearBit(String, byte)
   * @see #NUM_BITS
   */
  public byte setBit(String bitName, byte holder) {
    return changeBit(bitName, true, holder);
  }

  /**
   * Clears the bit position associated with the given name to <code>false</code>.<br>
   * <br>
   * <b>Example:</b><code>
   * <pre>
   *  byte holder = 64;
   *  holder = NamedBitMaskHelper.clearBit("myName", holder);
   * </pre>
   * </code>
   *
   * @param bitName
   *          The name of the bit. Must not be <code>null</code>.
   * @param holder
   *          The holder of the bits.
   * @return The new value with the given bit cleared.
   * @throws AssertionException
   *           if the given bitName is <code>null</code>.
   * @throws IllegalStateException
   *           if too many distinct bitNames are used in the application. Currently {@value #NUM_BITS} different
   *           bitNames are supported.
   * @see #changeBit(String, boolean, byte)
   * @see #setBit(String, byte)
   * @see #NUM_BITS
   */
  public byte clearBit(String bitName, byte holder) {
    return changeBit(bitName, false, holder);
  }

  /**
   * Checks if the bit position associated with the given name is set.<br>
   * <br>
   * <b>Example:</b><code>
   * <pre>
   *  byte holder = 0;
   *  holder = NamedBitMaskHelper.setBit("myName", holder);
   *  boolean isSet = NamedBitMaskHelper.isBitSet("myName", holder); // evaluates to true
   * </pre>
   * </code>
   *
   * @param bitName
   *          The name of the bit. Must not be <code>null</code>.
   * @param holder
   *          The holder of the bits.
   * @return <code>true</code> if the bit with the given name is set. <code>false</code> otherwise.
   */
  public boolean isBitSet(String bitName, byte holder) {
    return (holder & bitMaskFor(bitName)) != 0;
  }

  /**
   * Checks if the bit position associated with the given name has the given value.<br>
   * <br>
   * <b>Example:</b><code>
   * <pre>
   *  byte holder = 0;
   *  if(NamedBitMaskHelper.isBit("myName", holder, newValue)) {
   *    return; // no change
   *  }
   * </pre>
   * </code>
   *
   * @param bitName
   *          The name of the bit. Must not be <code>null</code>.
   * @param holder
   *          The holder of the bits.
   * @param expectedValue
   *          the value to check against.
   * @return <code>true</code> if the bit in given position has the given value. <code>false</code> otherwise.
   */
  public boolean isBit(String bitName, byte holder, boolean expectedValue) {
    return expectedValue == isBitSet(bitName, holder);
  }

  /**
   * Checks if the given byte has all bits set.
   *
   * @param holder
   *          The value holder to check.
   * @return <code>true</code> if all bits are set on the given holder.<code>false</code> otherwise.
   */
  public static boolean allBitsSet(byte holder) {
    return holder == ALL_BITS_SET;
  }

  /**
   * Gets the number of bit positions that are already used.
   *
   * @return The number of bits that have been used.
   */
  public synchronized int usedBits() {
    return m_nextIndex;
  }

  private byte bitMaskFor(String bitName) {
    Byte mask = m_masksByBitName.get(assertNotNull(bitName));
    if (mask != null) {
      return mask.byteValue();
    }

    synchronized (this) {
      mask = m_masksByBitName.get(bitName);
      if (mask != null) {
        return mask.byteValue();
      }
      return leaseNewMaskFor(bitName);
    }
  }

  private byte leaseNewMaskFor(String bitName) {
    if (m_nextIndex >= NUM_BITS) {
      throw new IllegalStateException("Too many entries. Currently only " + NUM_BITS + " entries are supported.");
    }
    byte result = bitMaskFor(m_nextIndex);
    m_nextIndex++;

    m_masksByBitName.put(bitName, Byte.valueOf(result));
    return result;
  }

  private static byte bitMaskFor(byte index) {
    return (byte) (1 << index);
  }
}
