// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface RasterFigureOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.RasterFigure)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.rplugininterop.RasterImage image = 1;</code>
   */
  boolean hasImage();
  /**
   * <code>.rplugininterop.RasterImage image = 1;</code>
   */
  org.jetbrains.r.rinterop.RasterImage getImage();
  /**
   * <code>.rplugininterop.RasterImage image = 1;</code>
   */
  org.jetbrains.r.rinterop.RasterImageOrBuilder getImageOrBuilder();

  /**
   * <code>.rplugininterop.AffinePoint from = 2;</code>
   */
  boolean hasFrom();
  /**
   * <code>.rplugininterop.AffinePoint from = 2;</code>
   */
  org.jetbrains.r.rinterop.AffinePoint getFrom();
  /**
   * <code>.rplugininterop.AffinePoint from = 2;</code>
   */
  org.jetbrains.r.rinterop.AffinePointOrBuilder getFromOrBuilder();

  /**
   * <code>.rplugininterop.AffinePoint to = 3;</code>
   */
  boolean hasTo();
  /**
   * <code>.rplugininterop.AffinePoint to = 3;</code>
   */
  org.jetbrains.r.rinterop.AffinePoint getTo();
  /**
   * <code>.rplugininterop.AffinePoint to = 3;</code>
   */
  org.jetbrains.r.rinterop.AffinePointOrBuilder getToOrBuilder();

  /**
   * <code>float angle = 4;</code>
   */
  float getAngle();

  /**
   * <code>bool interpolate = 5;</code>
   */
  boolean getInterpolate();
}
