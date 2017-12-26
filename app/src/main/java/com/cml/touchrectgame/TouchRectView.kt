package com.cml.touchrectgame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.concurrent.thread

/**
 * author : cml on 2017/12/25
 * github : https://github.com/cmlgithub
 */
class TouchRectView @JvmOverloads constructor(context: Context,attrs: AttributeSet? = null,defStyleAttr: Int =0): View(context,attrs,defStyleAttr) {

    val TAG = "CML"

    var widthView: Float = 0f
    var heightView: Float = 0f
    //小球和擋板
    var cx = 0f
    var radius = 0f
    var cy = 0f
    var mCirclePaint: Paint? = null
    var mRectBafflePaint: Paint? = null


    var baffleLeft: Float = 0f
    var baffleTop: Float = 0f
    var baffleRight: Float = 0f
    var baffleBottom: Float = 0f

    //方塊
    var brickList: MutableList<BeanRect> = mutableListOf()
    var brickWidth = 0
    var brickHeight = 0
    var row = 4
    var column = 5
    var mRectBrickPaint: Paint ? = null
    var isImapctSize = 0

    //文字
    var mTextPaint: Paint? = null

    var firstDraw: Boolean = true
    var isGameOver: Boolean = false

    init {
        mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCirclePaint!!.color = resources.getColor(R.color.colorAccent)

        mRectBafflePaint = Paint()
        mRectBafflePaint!!.color = resources.getColor(R.color.colorPrimaryDark)

        mRectBrickPaint = Paint()

        mTextPaint = Paint()
        mTextPaint!!.color = Color.WHITE
        mTextPaint!!.textSize = 100f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthView = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        heightView = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        radius = widthView /25
        cx = widthView /2
        cy = heightView *3/4

        baffleLeft = cx-2*radius
        baffleTop = cy+radius
        baffleRight = cx+2*radius
        baffleBottom = cy+radius+radius/2
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        brickWidth = widthView.toInt()/5
        brickHeight = brickWidth /2

        for (i in 0..row){
            for (j in 0..column){
                brickList.add(createBrick(i,j))
            }
        }
    }

    var drawText = "GameOver"
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas != null){

            if(isGameOver){
                canvas.drawText(drawText,widthView/2,heightView/2,mTextPaint)
                return
            }

            canvas.drawCircle(cx,cy,radius, mCirclePaint)

            if(firstDraw){
                canvas.drawRect(baffleLeft,baffleTop,baffleRight,baffleBottom, mRectBafflePaint)
            }else{
                baffleLeft = rectX-2*radius
                baffleRight = rectX+2*radius
                if(baffleLeft < 0){
                    baffleLeft = 0f
                    baffleRight = 4*radius
                }
                if(baffleRight > widthView){
                    baffleRight = widthView
                    baffleLeft = widthView - 4*radius;
                }
                canvas.drawRect(baffleLeft,baffleTop,baffleRight,baffleBottom, mRectBafflePaint)
            }

            for (x in brickList){
                if(!x.isImpact){
                    mRectBrickPaint!!.color = Color.parseColor(x.fillColor)
                    canvas.drawRect(x.rect, mRectBrickPaint)
                }
            }
        }
    }

    var rectX: Float = 0f

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        firstDraw = false
        when(event!!.action){
            MotionEvent.ACTION_DOWN ->{
                rectX = event.rawX
                smallBallRun()
            }
            MotionEvent.ACTION_MOVE ->{
                rectX = event.rawX
            }
        }
        postInvalidate()
        return true
    }


    var x = 1
    var y = 1
    fun smallBallRun(){
        thread {
            while (!isGameOver){
                if(cx + radius >= widthView){//turn left
                    x = -1
                }
                if(cx - radius<= 0){//turn right
                    x = 1
                }
//                if(x == -1){
//                    if(cy >= baffleTop && cy <= baffleBottom){
//                        if(cx - radius <= baffleRight){
//                            x = 1
//                        }
//                    }
//                }
//
//                if(x == 1){
//                    if(cy >= baffleTop && cy <= baffleBottom){
//                        if(cx + radius >= baffleLeft){
//                            x = -1
//                        }
//                    }
//                }

                cx = cx+5*x
                if(cy - radius <= 0 ){//turn bottom
                    y = -1
                }

                if(cy + radius>= heightView){//game over
                    isGameOver = true
                }

                if(y == -1){
                    if(cx >= baffleLeft && cx <= baffleRight){
                        if(cy + radius >= baffleTop){
                            y = 1
                        }
                    }
                }
                cy = cy-5*y
                Thread.sleep(50)
                postInvalidate()
                for (brick in brickList){
                    if(cx >= brick.rect!!.left && cx <= brick.rect!!.right){//bottom
                        if(cy - radius <= brick.rect!!.bottom && !brick.isImpact ){
                            brick.isImpact = true
                            isImapctSize++
                            y = -1
                        }

//                        if(cy + radius >= brick.rect!!.top){
//                            brick.isImpact = true
//                            y = 1
//                        }
                    }

                    if(cy >= brick.rect!!.top && cy <= brick.rect!!.bottom){
                        if(cx - radius <= brick.rect!!.right && !brick.isImpact ){
                            brick.isImpact = true
                            isImapctSize++
                            x = 1
                        }
//                        if(cx + radius >= brick.rect!!.left){
//                            brick.isImpact = true
//                            x = -1
//                        }
                    }
                }
                if(isImapctSize == brickList.size){
                    isGameOver = true
                    drawText = "success"
                    postInvalidate()
                }
            }
        }.start()
    }


    fun createBrick(r:Int,c: Int): BeanRect{
        var brick = BeanRect()
        var rectf = RectF()
        rectf.left = (c*brickWidth).toFloat()
        rectf.top = (r*brickHeight).toFloat()
        rectf.right = (c*brickWidth+brickWidth).toFloat()
        rectf.bottom = (r*brickHeight+brickHeight).toFloat()

        brick.fillColor = "#" + Integer.toHexString((-16777216 * Math.random()).toInt())
        brick.rect = rectf
        return brick
    }

}