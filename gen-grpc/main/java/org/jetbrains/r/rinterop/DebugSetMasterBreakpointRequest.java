// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

/**
 * Protobuf type {@code rplugininterop.DebugSetMasterBreakpointRequest}
 */
public final class DebugSetMasterBreakpointRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:rplugininterop.DebugSetMasterBreakpointRequest)
    DebugSetMasterBreakpointRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use DebugSetMasterBreakpointRequest.newBuilder() to construct.
  private DebugSetMasterBreakpointRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private DebugSetMasterBreakpointRequest() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new DebugSetMasterBreakpointRequest();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private DebugSetMasterBreakpointRequest(
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
          case 8: {

            breakpointId_ = input.readInt32();
            break;
          }
          case 18: {
            com.google.protobuf.Empty.Builder subBuilder = null;
            if (masterCase_ == 2) {
              subBuilder = ((com.google.protobuf.Empty) master_).toBuilder();
            }
            master_ =
                input.readMessage(com.google.protobuf.Empty.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom((com.google.protobuf.Empty) master_);
              master_ = subBuilder.buildPartial();
            }
            masterCase_ = 2;
            break;
          }
          case 24: {
            masterCase_ = 3;
            master_ = input.readInt32();
            break;
          }
          case 32: {

            leaveEnabled_ = input.readBool();
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
    return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_DebugSetMasterBreakpointRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_DebugSetMasterBreakpointRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest.class, org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest.Builder.class);
  }

  private int masterCase_ = 0;
  private java.lang.Object master_;
  public enum MasterCase
      implements com.google.protobuf.Internal.EnumLite,
          com.google.protobuf.AbstractMessage.InternalOneOfEnum {
    NONE(2),
    MASTERID(3),
    MASTER_NOT_SET(0);
    private final int value;
    private MasterCase(int value) {
      this.value = value;
    }
    /**
     * @param value The number of the enum to look for.
     * @return The enum associated with the given number.
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @java.lang.Deprecated
    public static MasterCase valueOf(int value) {
      return forNumber(value);
    }

    public static MasterCase forNumber(int value) {
      switch (value) {
        case 2: return NONE;
        case 3: return MASTERID;
        case 0: return MASTER_NOT_SET;
        default: return null;
      }
    }
    public int getNumber() {
      return this.value;
    }
  };

  public MasterCase
  getMasterCase() {
    return MasterCase.forNumber(
        masterCase_);
  }

  public static final int BREAKPOINTID_FIELD_NUMBER = 1;
  private int breakpointId_;
  /**
   * <code>int32 breakpointId = 1;</code>
   * @return The breakpointId.
   */
  @java.lang.Override
  public int getBreakpointId() {
    return breakpointId_;
  }

  public static final int NONE_FIELD_NUMBER = 2;
  /**
   * <code>.google.protobuf.Empty none = 2;</code>
   * @return Whether the none field is set.
   */
  @java.lang.Override
  public boolean hasNone() {
    return masterCase_ == 2;
  }
  /**
   * <code>.google.protobuf.Empty none = 2;</code>
   * @return The none.
   */
  @java.lang.Override
  public com.google.protobuf.Empty getNone() {
    if (masterCase_ == 2) {
       return (com.google.protobuf.Empty) master_;
    }
    return com.google.protobuf.Empty.getDefaultInstance();
  }
  /**
   * <code>.google.protobuf.Empty none = 2;</code>
   */
  @java.lang.Override
  public com.google.protobuf.EmptyOrBuilder getNoneOrBuilder() {
    if (masterCase_ == 2) {
       return (com.google.protobuf.Empty) master_;
    }
    return com.google.protobuf.Empty.getDefaultInstance();
  }

  public static final int MASTERID_FIELD_NUMBER = 3;
  /**
   * <code>int32 masterId = 3;</code>
   * @return Whether the masterId field is set.
   */
  @java.lang.Override
  public boolean hasMasterId() {
    return masterCase_ == 3;
  }
  /**
   * <code>int32 masterId = 3;</code>
   * @return The masterId.
   */
  @java.lang.Override
  public int getMasterId() {
    if (masterCase_ == 3) {
      return (java.lang.Integer) master_;
    }
    return 0;
  }

  public static final int LEAVEENABLED_FIELD_NUMBER = 4;
  private boolean leaveEnabled_;
  /**
   * <code>bool leaveEnabled = 4;</code>
   * @return The leaveEnabled.
   */
  @java.lang.Override
  public boolean getLeaveEnabled() {
    return leaveEnabled_;
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
    if (breakpointId_ != 0) {
      output.writeInt32(1, breakpointId_);
    }
    if (masterCase_ == 2) {
      output.writeMessage(2, (com.google.protobuf.Empty) master_);
    }
    if (masterCase_ == 3) {
      output.writeInt32(
          3, (int)((java.lang.Integer) master_));
    }
    if (leaveEnabled_ != false) {
      output.writeBool(4, leaveEnabled_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (breakpointId_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, breakpointId_);
    }
    if (masterCase_ == 2) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, (com.google.protobuf.Empty) master_);
    }
    if (masterCase_ == 3) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(
            3, (int)((java.lang.Integer) master_));
    }
    if (leaveEnabled_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(4, leaveEnabled_);
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
    if (!(obj instanceof org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest)) {
      return super.equals(obj);
    }
    org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest other = (org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest) obj;

    if (getBreakpointId()
        != other.getBreakpointId()) return false;
    if (getLeaveEnabled()
        != other.getLeaveEnabled()) return false;
    if (!getMasterCase().equals(other.getMasterCase())) return false;
    switch (masterCase_) {
      case 2:
        if (!getNone()
            .equals(other.getNone())) return false;
        break;
      case 3:
        if (getMasterId()
            != other.getMasterId()) return false;
        break;
      case 0:
      default:
    }
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
    hash = (37 * hash) + BREAKPOINTID_FIELD_NUMBER;
    hash = (53 * hash) + getBreakpointId();
    hash = (37 * hash) + LEAVEENABLED_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getLeaveEnabled());
    switch (masterCase_) {
      case 2:
        hash = (37 * hash) + NONE_FIELD_NUMBER;
        hash = (53 * hash) + getNone().hashCode();
        break;
      case 3:
        hash = (37 * hash) + MASTERID_FIELD_NUMBER;
        hash = (53 * hash) + getMasterId();
        break;
      case 0:
      default:
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parseFrom(
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
  public static Builder newBuilder(org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest prototype) {
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
   * Protobuf type {@code rplugininterop.DebugSetMasterBreakpointRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:rplugininterop.DebugSetMasterBreakpointRequest)
      org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_DebugSetMasterBreakpointRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_DebugSetMasterBreakpointRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest.class, org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest.Builder.class);
    }

    // Construct using org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest.newBuilder()
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
      breakpointId_ = 0;

      leaveEnabled_ = false;

      masterCase_ = 0;
      master_ = null;
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_DebugSetMasterBreakpointRequest_descriptor;
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest getDefaultInstanceForType() {
      return org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest.getDefaultInstance();
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest build() {
      org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest buildPartial() {
      org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest result = new org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest(this);
      result.breakpointId_ = breakpointId_;
      if (masterCase_ == 2) {
        if (noneBuilder_ == null) {
          result.master_ = master_;
        } else {
          result.master_ = noneBuilder_.build();
        }
      }
      if (masterCase_ == 3) {
        result.master_ = master_;
      }
      result.leaveEnabled_ = leaveEnabled_;
      result.masterCase_ = masterCase_;
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
      if (other instanceof org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest) {
        return mergeFrom((org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest other) {
      if (other == org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest.getDefaultInstance()) return this;
      if (other.getBreakpointId() != 0) {
        setBreakpointId(other.getBreakpointId());
      }
      if (other.getLeaveEnabled() != false) {
        setLeaveEnabled(other.getLeaveEnabled());
      }
      switch (other.getMasterCase()) {
        case NONE: {
          mergeNone(other.getNone());
          break;
        }
        case MASTERID: {
          setMasterId(other.getMasterId());
          break;
        }
        case MASTER_NOT_SET: {
          break;
        }
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
      org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int masterCase_ = 0;
    private java.lang.Object master_;
    public MasterCase
        getMasterCase() {
      return MasterCase.forNumber(
          masterCase_);
    }

    public Builder clearMaster() {
      masterCase_ = 0;
      master_ = null;
      onChanged();
      return this;
    }


    private int breakpointId_ ;
    /**
     * <code>int32 breakpointId = 1;</code>
     * @return The breakpointId.
     */
    @java.lang.Override
    public int getBreakpointId() {
      return breakpointId_;
    }
    /**
     * <code>int32 breakpointId = 1;</code>
     * @param value The breakpointId to set.
     * @return This builder for chaining.
     */
    public Builder setBreakpointId(int value) {
      
      breakpointId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 breakpointId = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearBreakpointId() {
      
      breakpointId_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Empty, com.google.protobuf.Empty.Builder, com.google.protobuf.EmptyOrBuilder> noneBuilder_;
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     * @return Whether the none field is set.
     */
    @java.lang.Override
    public boolean hasNone() {
      return masterCase_ == 2;
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     * @return The none.
     */
    @java.lang.Override
    public com.google.protobuf.Empty getNone() {
      if (noneBuilder_ == null) {
        if (masterCase_ == 2) {
          return (com.google.protobuf.Empty) master_;
        }
        return com.google.protobuf.Empty.getDefaultInstance();
      } else {
        if (masterCase_ == 2) {
          return noneBuilder_.getMessage();
        }
        return com.google.protobuf.Empty.getDefaultInstance();
      }
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     */
    public Builder setNone(com.google.protobuf.Empty value) {
      if (noneBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        master_ = value;
        onChanged();
      } else {
        noneBuilder_.setMessage(value);
      }
      masterCase_ = 2;
      return this;
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     */
    public Builder setNone(
        com.google.protobuf.Empty.Builder builderForValue) {
      if (noneBuilder_ == null) {
        master_ = builderForValue.build();
        onChanged();
      } else {
        noneBuilder_.setMessage(builderForValue.build());
      }
      masterCase_ = 2;
      return this;
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     */
    public Builder mergeNone(com.google.protobuf.Empty value) {
      if (noneBuilder_ == null) {
        if (masterCase_ == 2 &&
            master_ != com.google.protobuf.Empty.getDefaultInstance()) {
          master_ = com.google.protobuf.Empty.newBuilder((com.google.protobuf.Empty) master_)
              .mergeFrom(value).buildPartial();
        } else {
          master_ = value;
        }
        onChanged();
      } else {
        if (masterCase_ == 2) {
          noneBuilder_.mergeFrom(value);
        }
        noneBuilder_.setMessage(value);
      }
      masterCase_ = 2;
      return this;
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     */
    public Builder clearNone() {
      if (noneBuilder_ == null) {
        if (masterCase_ == 2) {
          masterCase_ = 0;
          master_ = null;
          onChanged();
        }
      } else {
        if (masterCase_ == 2) {
          masterCase_ = 0;
          master_ = null;
        }
        noneBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     */
    public com.google.protobuf.Empty.Builder getNoneBuilder() {
      return getNoneFieldBuilder().getBuilder();
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     */
    @java.lang.Override
    public com.google.protobuf.EmptyOrBuilder getNoneOrBuilder() {
      if ((masterCase_ == 2) && (noneBuilder_ != null)) {
        return noneBuilder_.getMessageOrBuilder();
      } else {
        if (masterCase_ == 2) {
          return (com.google.protobuf.Empty) master_;
        }
        return com.google.protobuf.Empty.getDefaultInstance();
      }
    }
    /**
     * <code>.google.protobuf.Empty none = 2;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.google.protobuf.Empty, com.google.protobuf.Empty.Builder, com.google.protobuf.EmptyOrBuilder> 
        getNoneFieldBuilder() {
      if (noneBuilder_ == null) {
        if (!(masterCase_ == 2)) {
          master_ = com.google.protobuf.Empty.getDefaultInstance();
        }
        noneBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.google.protobuf.Empty, com.google.protobuf.Empty.Builder, com.google.protobuf.EmptyOrBuilder>(
                (com.google.protobuf.Empty) master_,
                getParentForChildren(),
                isClean());
        master_ = null;
      }
      masterCase_ = 2;
      onChanged();;
      return noneBuilder_;
    }

    /**
     * <code>int32 masterId = 3;</code>
     * @return Whether the masterId field is set.
     */
    public boolean hasMasterId() {
      return masterCase_ == 3;
    }
    /**
     * <code>int32 masterId = 3;</code>
     * @return The masterId.
     */
    public int getMasterId() {
      if (masterCase_ == 3) {
        return (java.lang.Integer) master_;
      }
      return 0;
    }
    /**
     * <code>int32 masterId = 3;</code>
     * @param value The masterId to set.
     * @return This builder for chaining.
     */
    public Builder setMasterId(int value) {
      masterCase_ = 3;
      master_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 masterId = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearMasterId() {
      if (masterCase_ == 3) {
        masterCase_ = 0;
        master_ = null;
        onChanged();
      }
      return this;
    }

    private boolean leaveEnabled_ ;
    /**
     * <code>bool leaveEnabled = 4;</code>
     * @return The leaveEnabled.
     */
    @java.lang.Override
    public boolean getLeaveEnabled() {
      return leaveEnabled_;
    }
    /**
     * <code>bool leaveEnabled = 4;</code>
     * @param value The leaveEnabled to set.
     * @return This builder for chaining.
     */
    public Builder setLeaveEnabled(boolean value) {
      
      leaveEnabled_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bool leaveEnabled = 4;</code>
     * @return This builder for chaining.
     */
    public Builder clearLeaveEnabled() {
      
      leaveEnabled_ = false;
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


    // @@protoc_insertion_point(builder_scope:rplugininterop.DebugSetMasterBreakpointRequest)
  }

  // @@protoc_insertion_point(class_scope:rplugininterop.DebugSetMasterBreakpointRequest)
  private static final org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest();
  }

  public static org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<DebugSetMasterBreakpointRequest>
      PARSER = new com.google.protobuf.AbstractParser<DebugSetMasterBreakpointRequest>() {
    @java.lang.Override
    public DebugSetMasterBreakpointRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new DebugSetMasterBreakpointRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<DebugSetMasterBreakpointRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<DebugSetMasterBreakpointRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.jetbrains.r.rinterop.DebugSetMasterBreakpointRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

