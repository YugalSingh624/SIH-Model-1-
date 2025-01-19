package com.example.sih



import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.PoseLandmark


class OverlayView(context: Context?,attrs:AttributeSet?):View(context, attrs) {

    private val paint =Paint().apply {
        color = Color.GREEN
        strokeWidth = 8f
        style =Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private var poseLandmark:List<PoseLandmark>? = null

    fun setPoseLandmarks(landmark: List<PoseLandmark>)
    {
        this.poseLandmark = landmark
        invalidate()
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)
        poseLandmark?.let { landmarks ->
            for(landmark in landmarks){
                canvas?.drawCircle(landmark.position.x , landmark.position.y,10f,paint)
            }
        val leftShoulder = landmarks.find {it.landmarkType == PoseLandmark.LEFT_SHOULDER }
            val rightShoulder = landmarks.find { it.landmarkType ==PoseLandmark.RIGHT_SHOULDER }
            if(leftShoulder !=null && rightShoulder!=null){
                canvas?.drawLine(
                    leftShoulder.position.x ,leftShoulder.position.y,
                    rightShoulder.position.x,rightShoulder.position.y,
                    linePaint
                )
            }
        }
    }
}