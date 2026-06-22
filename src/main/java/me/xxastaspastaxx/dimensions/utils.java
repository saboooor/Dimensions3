package me.xxastaspastaxx.dimensions;

public class utils {
  // private static final byte TEXT_DISPLAY_FLAG_SHADOW = 0x01;
  // private static final byte TEXT_DISPLAY_FLAG_SEE_THROUGH = 0x02;
  // private static final byte TEXT_DISPLAY_FLAG_DEFAULT_BACKGROUND = 0x04;
  private static final byte TEXT_DISPLAY_FLAG_ALIGN_LEFT = 0x08;
  private static final byte TEXT_DISPLAY_FLAG_ALIGN_RIGHT = 0x10;

  private static final byte TEXT_DISPLAY_ALIGNMENT_MASK =
      TEXT_DISPLAY_FLAG_ALIGN_LEFT | TEXT_DISPLAY_FLAG_ALIGN_RIGHT;

  public enum TextAlignment {
    CENTER,
    LEFT,
    RIGHT
  }

  public static byte setTextAlignment(byte flags, TextAlignment alignment) {
    flags = (byte) (flags & ~TEXT_DISPLAY_ALIGNMENT_MASK);

    return switch (alignment) {
      case CENTER -> flags;
      case LEFT -> (byte) (flags | TEXT_DISPLAY_FLAG_ALIGN_LEFT);
      case RIGHT -> (byte) (flags | TEXT_DISPLAY_FLAG_ALIGN_RIGHT);
    };
  }

  public static int packBrightness(int blockLight, int skyLight) {
    if (blockLight < 0 || blockLight > 15) {
      throw new IllegalArgumentException("blockLight must be between 0 and 15");
    }

    if (skyLight < 0 || skyLight > 15) {
      throw new IllegalArgumentException("skyLight must be between 0 and 15");
    }

    return (blockLight << 4) | (skyLight << 20);
  }
}
