package com.example.donappt5.data.util

import android.content.Context
import android.graphics.Color
import com.example.donappt5.data.model.MyClusterItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class MyClusterRenderer(
    context: Context?, map: GoogleMap?,
    clusterManager: ClusterManager<MyClusterItem?>?
) : DefaultClusterRenderer<MyClusterItem?>(context, map, clusterManager) {
    override fun getColor(clusterSize: Int): Int {
        return Color.parseColor("#000000")
    }

    override fun onBeforeClusterItemRendered(
        item: MyClusterItem,
        markerOptions: MarkerOptions
    ) {
        val markerDescriptor =
            BitmapDescriptorFactory.defaultMarker(0.0f) //TODO stylize to black
        markerOptions.icon(markerDescriptor)
    }
}