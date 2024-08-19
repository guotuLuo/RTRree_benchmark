package com.example.entity;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.geotools.api.referencing.operation.MathTransform;

import java.util.stream.StreamSupport;


public class CirclePolygon {
    // 将MathTransform对象缓存起来，避免重复创建

    private static MathTransform transformToProjected;

    private  static MathTransform transformToGeographic;

    static {
        try {
            // 初始化 MathTransform 对象
            CoordinateReferenceSystem geographicCRS = CRS.decode("EPSG:4326"); // WGS84
            CoordinateReferenceSystem projectedCRS = CRS.decode("EPSG:3857"); // Web Mercator

            // 创建从地理坐标到投影坐标的转换器
            transformToProjected = CRS.findMathTransform(geographicCRS, projectedCRS, true);
            // 创建从投影坐标到地理坐标的转换器
            transformToGeographic = CRS.findMathTransform(projectedCRS, geographicCRS, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Polygon getCirclePolygon(double circleLat, double circleLon, double radiusInMeters) {
        try {

            GeometryFactory geometryFactory = new GeometryFactory();
            // 将圆心转换为投影坐标系
            Point centerPoint = geometryFactory.createPoint(new Coordinate(circleLon, circleLat));
            Point projectedCenterPoint = (Point) JTS.transform(centerPoint, transformToProjected);
            // 在投影坐标系中生成圆形缓冲区
            Polygon bufferPolygon = (Polygon) projectedCenterPoint.buffer(radiusInMeters);
            // 将缓冲区转换回地理坐标系（WGS84）
            Polygon geographicBufferPolygon = (Polygon) JTS.transform(bufferPolygon, transformToGeographic);

            return geographicBufferPolygon;
        } catch (Exception e) {
            // System.err.println("Error generating circle polygon: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}