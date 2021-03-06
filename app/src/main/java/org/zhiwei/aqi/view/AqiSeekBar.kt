package org.zhiwei.aqi.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StyleRes
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import org.zhiwei.aqi.R
import org.zhiwei.libcore.LogKt


/**
 * 作者： 志威  zhiwei.org
 * 主页： Github: https://github.com/zhiwei1990
 * 日期： 2020年05月21日 17:12
 * 签名： 天行健，君子以自强不息；地势坤，君子以厚德载物。
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/  -- 志威 zhiwei.org
 *
 * You never know what you can do until you try !
 * ----------------------------------------------------------------
 * 用于显示aqi当前状态位置的自定义控件
 */
class AqiSeekBar : View {

	constructor(context: Context) : super(context) {
		initConfig(context, null, 0, 0)
	}

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

		initConfig(context, attrs, 0, 0)
	}

	constructor(context: Context, attrs: AttributeSet?, defaultAttr: Int) : super(
		context,
		attrs,
		defaultAttr
	) {
		initConfig(context, attrs, defaultAttr, 0)
	}

	constructor(
		context: Context,
		attrs: AttributeSet?,
		defaultAttr: Int,
		@StyleRes defStyleRes: Int
	) : super(
		context,
		attrs,
		defaultAttr,
		defStyleRes
	) {
		initConfig(context, attrs, defaultAttr, defStyleRes)
	}

	//region 成员属性

	//画笔
	private val bgPaint = Paint()
	private val floaterPaint = Paint()
	private val tvPaint = Paint()

	private var barHeight = 15f//aqiSeekBar的背景条的高度
	private var barWidth = 0f//aqiSeekBar的宽度

	//aqi等级的color标准值

	//colors
	private val AQI_COLOR_GOOD = Color.parseColor("#009966")
	private val AQI_COLOR_MODERATE = Color.parseColor("#FFDE33")
	private val AQI_COLOR_USG = Color.parseColor("#FF9933")
	private val AQI_COLOR_UNHEALTHY = Color.parseColor("#CC0033")
	private val AQI_COLOR_VERY_UNHEALTHY = Color.parseColor("#660099")
	private val AQI_COLOR_HAZARDOUS = Color.parseColor("#7E0023")

	private val aqiColorLevels = intArrayOf(
		Color.parseColor("#009966"),
		Color.parseColor("#FFDE33"),
		Color.parseColor("#FF9933"),
		Color.parseColor("#CC0033"),
		Color.parseColor("#660099"),
		Color.parseColor("#7E0023")
	)
	private val aqiColorPos = floatArrayOf(0.166f, 0.33f, 0.5f, 0.66f, 0.83f, 1f)

	private var aqiLevelText = AQI_LEVEL_TEXT_GOOD//浮标文字
	private var aqiTvPos: Float = 0f//浮标的文字的位置
	private var aqiTvWidth: Float//浮标文字的宽度

	private var floaterPosition = 0f//浮标的圆心位置
	private var floaterPosRatio = 0f//浮标对比bar的宽度的位置比例
	private var floaterRadius = 40f//浮标圆半径
	private var floaterRadiusRatio = 0.04f//浮标半径，对比bar的宽度的比例


	//endregion

	private fun initConfig(
		context: Context,
		attrs: AttributeSet?,
		defaultAttr: Int,
		defStyleRes: Int
	) {
		val typedArray = context.theme.obtainStyledAttributes(
			attrs,
			R.styleable.AqiSeekBar,
			defaultAttr,
			defStyleRes
		)

	}


	init {
		//背景画笔
		bgPaint.color = Color.BLUE
		bgPaint.style = Paint.Style.FILL
		bgPaint.strokeWidth = 2f
		bgPaint.isAntiAlias = true
		//文字画笔
		tvPaint.color = Color.WHITE
		tvPaint.textSize = 36f
		tvPaint.strokeWidth = 2f
		tvPaint.style = Paint.Style.FILL

		aqiTvWidth = tvPaint.measureText(aqiLevelText)

		aqiTvPos = (width - aqiTvWidth) / 2

		//浮标画笔
		floaterPaint.color = aqiColorLevels[0]
		floaterPaint.style = Paint.Style.FILL
		floaterPaint.isAntiAlias = true
	}

	//region 函数api


	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		val widthSize = MeasureSpec.getSize(widthMeasureSpec)
		val widthMode = MeasureSpec.getMode(widthMeasureSpec)

		val heightSize = MeasureSpec.getSize(heightMeasureSpec)
		val heightMode = MeasureSpec.getMode(heightMeasureSpec)

		//实际宽高像素
		barWidth = (widthSize - paddingStart - paddingEnd).toFloat()

		LogKt.w("onMeasure 110行: width spec $widthMeasureSpec $widthMode $widthSize // height spec $heightMeasureSpec $heightMode $heightSize margin $marginStart $marginEnd  padding $paddingStart $paddingEnd")
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)

		barWidth = (w - paddingStart - paddingEnd).toFloat()
		floaterPosition = floaterPosRatio * barWidth
		floaterRadius = floaterRadiusRatio * barWidth
		if (floaterRadius > 40) {
			floaterRadius = 40f
		} else if (floaterRadius < 0) {
			floaterRadius = 0f
		}
		LogKt.i("onSizeChanged 115行: $w $oldw // $h  $oldh  margin $marginStart $marginEnd  padding $paddingStart $paddingEnd barWidth $barWidth")
	}


	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
		canvas ?: return
		//绘制背景,注意得到with，height padding等都是像素值
		val linearGradient =
			LinearGradient(0f, 0f, barWidth, 0f, aqiColorLevels, null, Shader.TileMode.CLAMP)
		bgPaint.shader = linearGradient
		canvas.drawRoundRect(
			paddingStart.toFloat(),
			(height - barHeight) / 2f,
			barWidth,
			(height + barHeight) / 2f,
			barHeight / 2f,
			barHeight / 2f,
			bgPaint
		)
		//浮标圆圈的绘制
		canvas.drawCircle(floaterPosition, height / 2f, floaterRadius, floaterPaint)
		//浮标中 绘制文字，需要计算文字大小，来确定baseline，才能准确的绘制位置
		val fontMetrics = tvPaint.fontMetrics
		val h = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
		canvas.drawText(aqiLevelText, floaterPosition - aqiTvWidth / 2, height / 2f + h, tvPaint)

		LogKt.d("onDraw 100行: h:$height w:$width marginStart:$marginStart marginEnd $marginEnd padding $paddingStart barWidth $barWidth 浮标位置,$floaterPosition")
	}

	override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
		super.onLayout(changed, left, top, right, bottom)

		LogKt.e("onLayout 139行: $changed $left $top $right $bottom")
	}

	//endregion

	fun setAqiNum(aqi: Int) {
		floaterPaint.color = genAqiColor(aqi)
		aqiLevelText = getAqiText(aqi)
		floaterPosition = calculateFloaterPosition(aqi)
		floaterPosRatio = floaterPosition / barWidth
		floaterRadiusRatio = floaterRadius / barWidth
		LogKt.e("setAqiNum 192: barWidth $barWidth 浮标位置,$floaterPosition")
		invalidate()
	}


	/**
	 * 根据aqi的数值，以及所属等级，计算出对应的颜色值
	 */
	private fun genAqiColor(aqi: Int): Int {
		return when (aqi) {
			in 0..50 -> {
				getCurrentColor(aqi / 50f, AQI_COLOR_GOOD, AQI_COLOR_MODERATE)
			}
			in 51..100 -> {
				getCurrentColor((aqi - 50) / 50f, AQI_COLOR_MODERATE, AQI_COLOR_USG)
			}
			in 101..150 -> {
				getCurrentColor((aqi - 100) / 50f, AQI_COLOR_USG, AQI_COLOR_UNHEALTHY)
			}
			in 151..200 -> {
				getCurrentColor((aqi - 150) / 50f, AQI_COLOR_UNHEALTHY, AQI_COLOR_VERY_UNHEALTHY)
			}
			in 200..300 -> {
				getCurrentColor((aqi - 200) / 100f, AQI_COLOR_VERY_UNHEALTHY, AQI_COLOR_HAZARDOUS)
			}
			in 300..Int.MAX_VALUE -> AQI_COLOR_HAZARDOUS
			else -> AQI_COLOR_GOOD
		}
	}

	/**
	 * 根据aqi数值，得到对应等级的描述文字
	 */
	private fun getAqiText(aqi: Int): String {
		return when (aqi) {
			in 0..50 -> AQI_LEVEL_TEXT_GOOD
			in 51..100 -> AQI_LEVEL_TEXT_MODERATE
			in 101..150 -> AQI_LEVEL_TEXT_UNHEALTHY_FOR_SENSITIVE_GROUP
			in 151..200 -> AQI_LEVEL_TEXT_UNHEALTHY
			in 200..300 -> AQI_LEVEL_TEXT_VERY_UNHEALTHY
			in 300..500 -> AQI_LEVEL_TEXT_HAZARDOUS
			in 500..Int.MAX_VALUE -> AQI_LEVEL_TEXT_VERY_HAZARDOUS
			else -> AQI_LEVEL_TEXT_UNKNOWN
		}
	}

	/**
	 * 根据aqi，计算浮标圆心大体位置
	 */
	private fun calculateFloaterPosition(aqi: Int): Float {
		//注意aqi为Int，所以aqi/500f 才能不为零。500是aqiSeekBar的量程大小
		// 在0--200其实是等分刻度的，200--300是两倍刻度，300--500是4倍刻度，比例尺是变化的。
		//额外都要加上 浮标的半径
		return when (aqi) {
			in 0..200 -> {
				//1个aqi单位 等于 barWidth/300
				floaterRadius / 2 + barWidth * (aqi / 300f)
			}
			in 200..300 -> {
				//在200之前的，如上，在200--300之内的，1个aqi单位 等于 barWidth/600
				floaterRadius / 2 + barWidth * ((3 * aqi - 200f) / 600f)
			}
			in 300..500 -> {
				//在300之前，计算规则如上，也就是分200 前后。300--500段，1个aqi单位 等于barWidth/1200
				floaterRadius / 2 + barWidth * ((7 * aqi - 700f) / 1200f)
			}
			in 500..Int.MAX_VALUE -> barWidth
			else -> 0f
		}
	}

	/**
	 * 根据过渡比例，来获取当前比例，在start和end之间的颜色值
	 */
	private fun getCurrentColor(fraction: Float, startColor: Int, endColor: Int): Int {
		val redStart = Color.red(startColor)
		val blueStart = Color.blue(startColor)
		val greenStart = Color.green(startColor)
		val alphaStart = Color.alpha(startColor)

		val redEnd = Color.red(endColor)
		val blueEnd = Color.blue(endColor)
		val greenEnd = Color.green(endColor)
		val alphaEnd = Color.alpha(endColor)

		val redDifference = redEnd - redStart
		val blueDifference = blueEnd - blueStart
		val greenDifference = greenEnd - greenStart
		val alphaDifference = alphaEnd - alphaStart

		val redCurrent = (redStart + fraction * redDifference).toInt()
		val blueCurrent = (blueStart + fraction * blueDifference).toInt()
		val greenCurrent = (greenStart + fraction * greenDifference).toInt()
		val alphaCurrent = (alphaStart + fraction * alphaDifference).toInt()
		return Color.argb(alphaCurrent, redCurrent, greenCurrent, blueCurrent)
	}

	companion object {
		private const val AQI_LEVEL_TEXT_GOOD = "优"
		private const val AQI_LEVEL_TEXT_MODERATE = "良"
		private const val AQI_LEVEL_TEXT_UNHEALTHY_FOR_SENSITIVE_GROUP = "差"
		private const val AQI_LEVEL_TEXT_UNHEALTHY = "劣"
		private const val AQI_LEVEL_TEXT_VERY_UNHEALTHY = "糟"
		private const val AQI_LEVEL_TEXT_HAZARDOUS = "危"//
		private const val AQI_LEVEL_TEXT_VERY_HAZARDOUS = "禁"
		private const val AQI_LEVEL_TEXT_UNKNOWN = "未"
	}

}