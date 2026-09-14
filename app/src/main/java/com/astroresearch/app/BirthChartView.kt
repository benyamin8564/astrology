package com.astroresearch.app

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.*

class BirthChartView(context:Context):View(context){
    var chart:Chart?=null
        set(value){ field=value; invalidate() }
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private val colors= intArrayOf(Color.rgb(210,70,70),Color.rgb(210,150,60),Color.rgb(70,150,90),Color.rgb(70,110,190))
    override fun onDraw(c:Canvas){ super.onDraw(c); val ch=chart?:return; val cx=width/2f; val cy=height/2f; val r=min(width,height)*.39f
        paint.style=Paint.Style.STROKE; paint.strokeWidth=2f; paint.color=Color.DKGRAY; c.drawCircle(cx,cy,r,paint); c.drawCircle(cx,cy,r*.72f,paint)
        for(i in 0..11){ val a=Math.toRadians(i*30.0-90); val x=(cx+cos(a)*r).toFloat(); val y=(cy+sin(a)*r).toFloat(); c.drawLine(cx,cy,x,y,paint); text(c,AstroCalculator.signs[i],cx+cos(a)*(r+24),cy+sin(a)*(r+24),14f,Paint.Align.CENTER) }
        paint.color=Color.GRAY; paint.strokeWidth=1f; c.drawLine(cx-r,cy,cx+r,cy,paint); c.drawLine(cx,cy-r,cx,cy+r,paint)
        text(c,"ASC ${AstroCalculator.sign(ch.asc)} ${AstroCalculator.deg(ch.asc)}",cx,cy+r+45,13f,Paint.Align.CENTER)
        text(c,"MC ${AstroCalculator.sign(ch.mc)} ${AstroCalculator.deg(ch.mc)}",cx,cy+r+63,13f,Paint.Align.CENTER)
        ch.bodies.forEachIndexed{idx,b-> val a=Math.toRadians(b.lon-90); val rr=r*.80; val x=(cx+cos(a)*rr).toFloat(); val y=(cy+sin(a)*rr).toFloat(); paint.style=Paint.Style.FILL; paint.color=colors[idx%colors.size]; c.drawCircle(x,y,5f,paint); text(c,b.name,x,y-9,12f,Paint.Align.CENTER) }
    }
    private fun text(c:Canvas,s:String,x:Float,y:Float,size:Float,align:Paint.Align){ paint.style=Paint.Style.FILL; paint.color=Color.DKGRAY; paint.textSize=size; paint.textAlign=align; c.drawText(s,x,y,paint) }
}
