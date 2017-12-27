package com.cml.touchrectgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.concurrent.thread

/**
 * Created by chenmingliang on 2017/12/26.
 */
class ImpactDismissGameView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    var TAG = "CML"


    //viewSelf
    private var widthView = 0f
    private var heightView = 0f
    private var widthViewHalf = 0f
    private var heightViewHalf = 0f

    //start Button
    private var isStart = false
    private var isClickDownStartButton = false
    private var startButtonWidth = 200
    private var startButtonHeight = 100
    private var startButtonWidthHalf = startButtonWidth/2
    private var startButtonHeightHalf = startButtonHeight/2
    private var mStartButtonPaint: Paint? = null

    //start Text
    private var startText = "Start"
    private var mStartTextPaint: Paint? = null
    private var startTextFontMetrics: Paint.FontMetrics? = null

    //ball
    private var ballCenterX = 0f
    private var ballCenterY = 0f
    private var mBallPaint: Paint? = null
    private var ballRadius = 0f

    //baffle
    private var baffleWidth = 0f
    private var baffleHeight = 0f
    private var baffleLeft = 0f
    private var baffleTop = 0f
    private var baffleRight = 0f
    private var baffleBottom = 0f
    private var mBafflePaint: Paint? = null
    private var isClickDownBaffle = false

    //block
    private var row = 3
    private var column = 4
    private var blockWidth = 0f
    private var blockHeight = 0f
    private var blockList: MutableList<BeanRect> = mutableListOf()
    private var mBlockPaint: Paint? = null
    private var impactBlockSize = 0

    private var isGameOver = false


    init {
        mStartButtonPaint = Paint()
        mStartButtonPaint!!.color = context.resources.getColor(R.color.colorAccent)

        mStartTextPaint = Paint()
        mStartTextPaint!!.textAlign = Paint.Align.CENTER
        mStartTextPaint!!.color = Color.WHITE
        mStartTextPaint!!.textSize = 40f
        startTextFontMetrics = mStartTextPaint!!.fontMetrics

        mBallPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBallPaint!!.color = context.resources.getColor(R.color.colorAccent)

        mBafflePaint = Paint()
        mBafflePaint!!.color = context.resources.getColor(R.color.colorPrimary)

        mBlockPaint = Paint()
    }

    private var downX = 0f
    private var downY = 0f

    private var moveX = 0f

    private var isMoveBaffleLeft = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event!!.action){
            MotionEvent.ACTION_DOWN ->{
                downX = event.x
                moveX = event.x//record init x
                downY = event.y
                if(isStart){
                    isClickDownBaffle = downX >= baffleLeft && downX <= baffleLeft+baffleWidth
                            && downY >= baffleTop && downY <= baffleTop+baffleHeight
                }else{
                    isClickDownStartButton = downX >= widthViewHalf - startButtonWidthHalf && downX <= widthViewHalf + startButtonWidthHalf
                            && downY >= heightViewHalf - startButtonHeightHalf && downY <= heightViewHalf + startButtonHeightHalf

                    if(isClickDownStartButton){//add click change color
                        mStartButtonPaint!!.color = context.resources.getColor(R.color.colorAccentDark)
                        postInvalidate()
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                isMoveBaffleLeft = event.x - moveX < 0
                moveX = event.x

                if(!isBallRun){
                    xDirection = if(isMoveBaffleLeft) -1 else 1
                    ballRun()
                }

                if(isClickDownBaffle){//baffle move
                    baffleLeft = event.x - baffleWidth/2
                    baffleRight = baffleLeft + baffleWidth
                    if(baffleLeft <= 0){
                        baffleLeft = 0f
                        baffleRight = baffleWidth
                    }
                    if(baffleRight >= widthView){
                        baffleRight = widthView
                        baffleLeft = widthView-baffleWidth
                    }
                    postInvalidate()
                }
            }
            MotionEvent.ACTION_UP ->{
                if(isStart){

                }else{
                    if((impactBlockSize == blockList.size || isGameOver) && isClickDownStartButton){
                        if(mDismissFinish != null){
                            mDismissFinish!!.dismissFinish()
                            return true
                        }
                    }
                    if(event.x >= widthViewHalf - startButtonWidthHalf && event.x <= widthViewHalf + startButtonWidthHalf
                            && event.y >= heightViewHalf - startButtonHeightHalf && event.y <= heightViewHalf + startButtonHeightHalf
                            && isClickDownStartButton){
                        isClickDownStartButton = false
                        isStart = true
                        mStartButtonPaint!!.color = context.resources.getColor(R.color.colorAccent)
                        postInvalidate()
                    }
                }



            }
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        widthView = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        heightView = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        widthViewHalf = widthView / 2
        heightViewHalf = heightView / 2

        ballCenterX = widthViewHalf
        ballCenterY = heightView - heightView/5
        ballRadius = widthView/25

        baffleWidth = ballRadius * 4
        baffleHeight = ballRadius/2
        baffleLeft = ballCenterX - baffleWidth/2
        baffleTop = ballCenterY + ballRadius
        baffleRight = ballCenterX + baffleWidth/2
        baffleBottom = ballCenterY + ballRadius + baffleHeight

        blockWidth = widthView/5
        blockHeight = blockWidth/2

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initBlockList()
    }

    private fun initBlockList(){
        for (r in 0..row){
            for (c in 0..column){
                blockList!!.add(createBlock(r,c))
            }
        }
    }

    private fun createBlock(row:Int,column:Int): BeanRect {
        var block = BeanRect()
        block.fillColor = "#" + Integer.toHexString((-16777216 * Math.random()).toInt())
        val rectF = RectF()
        rectF.left = column*blockWidth
        rectF.top = row*blockHeight
        rectF.right = (1+column)*blockWidth
        rectF.bottom = (1+row)*blockHeight
        block.rect = rectF
        return block
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas == null) return

        if(impactBlockSize == blockList.size){
            startText = "Finish"
            drawStartButton(canvas)
            return
        }

        if(isGameOver){
            startText = "Over"
            drawStartButton(canvas)
            return
        }

        if(isStart){
            drawBallAndBaffle(canvas)
            drawBlock(canvas)
        }else{
            drawStartButton(canvas)
        }
    }


    // control ball run x,y direction
    private var xDirection = 1
    private var yDirection = -1
    private var isBallRun = false

    private fun ballRun(){
        isBallRun = true
        thread {
            loop@ while (true) {
                if(isDestory)
                    break
                ballCenterX += xDirection * 5
                ballCenterY += yDirection * 5
                if (ballCenterX + ballRadius >= widthView //impact view right
                        || ballCenterX - ballRadius <= 0  //impact view left
                        || (xDirection == 1 && ballCenterX < baffleLeft && ballCenterX + ballRadius >= baffleLeft && ballCenterY >= baffleTop && ballCenterY <= baffleBottom)//impact baffle left
                        || (xDirection == -1 && ballCenterX > baffleRight && ballCenterX - ballRadius <= baffleRight && ballCenterY >= baffleTop && ballCenterY <= baffleBottom)) {//impact baffle right
                    xDirection *= -1
                    sleepAndReDraw()
                    continue
                }
                if(ballCenterY + ballRadius >= heightView ){//impact view bottom
                    isGameOver = true
                    isStart = false
                    postInvalidate()
                    break@loop
                }

                if (ballCenterY - ballRadius <= 0   //impact view top
                        || (yDirection == 1 && ballCenterY + ballRadius >= baffleTop && ballCenterY < baffleTop && ballCenterX >= baffleLeft && ballCenterX <= baffleRight)//impact baffle top
                        || (yDirection == -1 && ballCenterY - ballRadius <= baffleBottom && ballCenterY > baffleBottom && ballCenterX >= baffleLeft && ballCenterX <= baffleRight)//impact baffle bottom
                        ) {
                    yDirection *= -1
                    sleepAndReDraw()
                    continue
                }
                for (block in blockList){
                    val blockRect = block.rect
                    if(((!block.isImpact)&&yDirection == -1 && ballCenterY > blockRect!!.bottom && ballCenterY-ballRadius <= blockRect.bottom && ballCenterX >= blockRect.left&& ballCenterX <= blockRect.right)//block bottom
                            || ((!block.isImpact)&&yDirection == 1 && ballCenterY < blockRect!!.top && ballCenterY + ballRadius >= blockRect.top && ballCenterX >= blockRect.left && ballCenterX <= blockRect.right)//block right
                            ){
                        yDirection *= -1
                        Log.e(TAG,"++")
                        block.isImpact = true
                        impactBlockSize++
                        sleepAndReDraw()
                        continue@loop
                    }
                    if(((!block.isImpact)&&xDirection == -1 && ballCenterX > blockRect!!.right && ballCenterX - ballRadius <= blockRect.right && ballCenterY >= blockRect.top && ballCenterY <= blockRect.bottom)//block right
                            ||((!block.isImpact)&&xDirection == 1 && ballCenterX < blockRect!!.left && ballCenterX + ballRadius >= blockRect.left && ballCenterY >= blockRect.top && ballCenterY <= blockRect.bottom)){//block Left
                        xDirection *= -1
                        Log.e(TAG,"++")
                        block.isImpact = true
                        impactBlockSize++
                        sleepAndReDraw()
                        continue@loop
                    }
                }
                sleepAndReDraw()
            }
        }.start()
    }

    private fun sleepAndReDraw(){
        Thread.sleep(15)
        postInvalidate()
    }

    private fun drawBlock(canvas: Canvas) {
        for (block in blockList){
            if(!block.isImpact){
                mBlockPaint!!.color = Color.parseColor(block.fillColor)
                canvas.drawRect(block.rect,mBlockPaint)
            }
        }
    }

    private fun drawBallAndBaffle(canvas: Canvas) {
        canvas.drawCircle(ballCenterX,ballCenterY,ballRadius,mBallPaint)
        canvas.drawRect(baffleLeft,baffleTop,baffleRight,baffleBottom,mBafflePaint)
    }

    private fun drawStartButton(canvas: Canvas) {
        canvas.drawRect(widthViewHalf - startButtonWidthHalf, heightViewHalf - startButtonHeightHalf, widthViewHalf + startButtonWidthHalf, heightViewHalf + startButtonHeightHalf, mStartButtonPaint)
        canvas.drawText(startText, widthViewHalf, heightViewHalf-startTextFontMetrics!!.top/2-startTextFontMetrics!!.bottom/2,mStartTextPaint)
    }

    private var isDestory = false
    public fun destoryView(){
        isDestory = true
    }



    private var mDismissFinish: DismissFinish? = null

    fun setDismissFinish(iaaa: DismissFinish) {
        mDismissFinish = iaaa
    }

    interface DismissFinish {
        fun dismissFinish()
    }

}