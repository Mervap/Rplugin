// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

/**
 * Protobuf type {@code rplugininterop.AffinePoint}
 */
public  final class AffinePoint extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:rplugininterop.AffinePoint)
    AffinePointOrBuilder {
private static final long serialVersionUID = 0L;
  // Use AffinePoint.newBuilder() to construct.
  private AffinePoint(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private AffinePoint() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new AffinePoint();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private AffinePoint(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 13: {

            xScale_ = input.readFloat();
            break;
          }
          case 21: {

            xOffset_ = input.readFloat();
            break;
          }
          case 29: {

            yScale_ = input.readFloat();
            break;
          }
          case 37: {

            yOffset_ = input.readFloat();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_AffinePoint_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_AffinePoint_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.jetbrains.r.rinterop.AffinePoint.class, org.jetbrains.r.rinterop.AffinePoint.Builder.class);
  }

  public static final int XSCALE_FIELD_NUMBER = 1;
  private float xScale_;
  /**
   * <code>float xScale = 1;</code>
   */
  public float getXScale() {
    return xScale_;
  }

  public static final int XOFFSET_FIELD_NUMBER = 2;
  private float xOffset_;
  /**
   * <code>float xOffset = 2;</code>
   */
  public float getXOffset() {
    return xOffset_;
  }

  public static final int YSCALE_FIELD_NUMBER = 3;
  private float yScale_;
  /**
   * <code>float yScale = 3;</code>
   */
  public float getYScale() {
    return yScale_;
  }

  public static final int YOFFSET_FIELD_NUMBER = 4;
  private float yOffset_;
  /**
   * <code>float yOffset = 4;</code>
   */
  public float getYOffset() {
    return yOffset_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (xScale_ != 0F) {
      output.writeFloat(1, xScale_);
    }
    if (xOffset_ != 0F) {
      output.writeFloat(2, xOffset_);
    }
    if (yScale_ != 0F) {
      output.writeFloat(3, yScale_);
    }
    if (yOffset_ != 0F) {
      output.writeFloat(4, yOffset_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (xScale_ != 0F) {
      size += com.google.protobuf.CodedOutputStream
        .computeFloatSize(1, xScale_);
    }
    if (xOffset_ != 0F) {
      size += com.google.protobuf.CodedOutputStream
        .computeFloatSize(2, xOffset_);
    }
    if (yScale_ != 0F) {
      size += com.google.protobuf.CodedOutputStream
        .computeFloatSize(3, yScale_);
    }
    if (yOffset_ != 0F) {
      size += com.google.protobuf.CodedOutputStream
        .computeFloatSize(4, yOffset_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.jetbrains.r.rinterop.AffinePoint)) {
      return super.equals(obj);
    }
    org.jetbrains.r.rinterop.AffinePoint other = (org.jetbrains.r.rinterop.AffinePoint) obj;

    if (java.lang.Float.floatToIntBits(getXScale())
        != java.lang.Float.floatToIntBits(
            other.getXScale())) return false;
    if (java.lang.Float.floatToIntBits(getXOffset())
        != java.lang.Float.floatToIntBits(
            other.getXOffset())) return false;
    if (java.lang.Float.floatToIntBits(getYScale())
        != java.lang.Float.floatToIntBits(
            other.getYScale())) return false;
    if (java.lang.Float.floatToIntBits(getYOffset())
        != java.lang.Float.floatToIntBits(
            other.getYOffset())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + XSCALE_FIELD_NUMBER;
    hash = (53 * hash) + java.lang.Float.floatToIntBits(
        getXScale());
    hash = (37 * hash) + XOFFSET_FIELD_NUMBER;
    hash = (53 * hash) + java.lang.Float.floatToIntBits(
        getXOffset());
    hash = (37 * hash) + YSCALE_FIELD_NUMBER;
    hash = (53 * hash) + java.lang.Float.floatToIntBits(
        getYScale());
    hash = (37 * hash) + YOFFSET_FIELD_NUMBER;
    hash = (53 * hash) + java.lang.Float.floatToIntBits(
        getYOffset());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.AffinePoint parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.jetbrains.r.rinterop.AffinePoint prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code rplugininterop.AffinePoint}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:rplugininterop.AffinePoint)
      org.jetbrains.r.rinterop.AffinePointOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_AffinePoint_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_AffinePoint_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.jetbrains.r.rinterop.AffinePoint.class, org.jetbrains.r.rinterop.AffinePoint.Builder.class);
    }

    // Construct using org.jetbrains.r.rinterop.AffinePoint.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      xScale_ = 0F;

      xOffset_ = 0F;

      yScale_ = 0F;

      yOffset_ = 0F;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_AffinePoint_descriptor;
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.AffinePoint getDefaultInstanceForType() {
      return org.jetbrains.r.rinterop.AffinePoint.getDefaultInstance();
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.AffinePoint build() {
      org.jetbrains.r.rinterop.AffinePoint result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.AffinePoint buildPartial() {
      org.jetbrains.r.rinterop.AffinePoint result = new org.jetbrains.r.rinterop.AffinePoint(this);
      result.xScale_ = xScale_;
      result.xOffset_ = xOffset_;
      result.yScale_ = yScale_;
      result.yOffset_ = yOffset_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.jetbrains.r.rinterop.AffinePoint) {
        return mergeFrom((org.jetbrains.r.rinterop.AffinePoint)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.jetbrains.r.rinterop.AffinePoint other) {
      if (other == org.jetbrains.r.rinterop.AffinePoint.getDefaultInstance()) return this;
      if (other.getXScale() != 0F) {
        setXScale(other.getXScale());
      }
      if (other.getXOffset() != 0F) {
        setXOffset(other.getXOffset());
      }
      if (other.getYScale() != 0F) {
        setYScale(other.getYScale());
      }
      if (other.getYOffset() != 0F) {
        setYOffset(other.getYOffset());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.jetbrains.r.rinterop.AffinePoint parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.r.rinterop.AffinePoint) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private float xScale_ ;
    /**
     * <code>float xScale = 1;</code>
     */
    public float getXScale() {
      return xScale_;
    }
    /**
     * <code>float xScale = 1;</code>
     */
    public Builder setXScale(float value) {
      
      xScale_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>float xScale = 1;</code>
     */
    public Builder clearXScale() {
      
      xScale_ = 0F;
      onChanged();
      return this;
    }

    private float xOffset_ ;
    /**
     * <code>float xOffset = 2;</code>
     */
    public float getXOffset() {
      return xOffset_;
    }
    /**
     * <code>float xOffset = 2;</code>
     */
    public Builder setXOffset(float value) {
      
      xOffset_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>float xOffset = 2;</code>
     */
    public Builder clearXOffset() {
      
      xOffset_ = 0F;
      onChanged();
      return this;
    }

    private float yScale_ ;
    /**
     * <code>float yScale = 3;</code>
     */
    public float getYScale() {
      return yScale_;
    }
    /**
     * <code>float yScale = 3;</code>
     */
    public Builder setYScale(float value) {
      
      yScale_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>float yScale = 3;</code>
     */
    public Builder clearYScale() {
      
      yScale_ = 0F;
      onChanged();
      return this;
    }

    private float yOffset_ ;
    /**
     * <code>float yOffset = 4;</code>
     */
    public float getYOffset() {
      return yOffset_;
    }
    /**
     * <code>float yOffset = 4;</code>
     */
    public Builder setYOffset(float value) {
      
      yOffset_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>float yOffset = 4;</code>
     */
    public Builder clearYOffset() {
      
      yOffset_ = 0F;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:rplugininterop.AffinePoint)
  }

  // @@protoc_insertion_point(class_scope:rplugininterop.AffinePoint)
  private static final org.jetbrains.r.rinterop.AffinePoint DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.jetbrains.r.rinterop.AffinePoint();
  }

  public static org.jetbrains.r.rinterop.AffinePoint getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<AffinePoint>
      PARSER = new com.google.protobuf.AbstractParser<AffinePoint>() {
    @java.lang.Override
    public AffinePoint parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new AffinePoint(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<AffinePoint> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<AffinePoint> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.jetbrains.r.rinterop.AffinePoint getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

