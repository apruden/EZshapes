package com.monolito.ezshapes

import android.graphics.Point
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.ParticleEffect
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import java.util.*


class EZShapesGame : ApplicationAdapter() {
    internal lateinit var part: SpriteBatch
    internal lateinit var targetRenderer: ShapeRenderer
    internal lateinit var shapeRenderers: HashMap<Shape, ShapeRenderer>
    internal lateinit var pe: ParticleEffect
    var currx = 0.0f
    var curry = 0.0f
    internal lateinit var targetShape: Shape
    internal var expFlag = false
    internal var started = false
    internal var dragging = false
    internal lateinit var shapes: List<Shape>
    internal lateinit var assetManager: AssetManager
    internal lateinit var gestureDetector: CustomGestureDetector
    var W = 0
    var H = 0
    internal var L = 0

    override fun create() {
        gestureDetector = CustomGestureDetector()
        Gdx.input.inputProcessor = GestureDetector(gestureDetector)
        part = SpriteBatch()
        targetRenderer = ShapeRenderer()
        shapeRenderers = HashMap()
        W = Gdx.graphics.width
        H = Gdx.graphics.height
        L = Math.min(W, H) / 10

        currx = W / 2f
        curry = H / 2f
        shapes = listOf(Shape(ShapeType.CIRCLE,
                Point(2 * L, 2 * L),
                Color.YELLOW,
                { s -> s.circle(0f, 0f, 1f * L) }),
                Shape(ShapeType.STAR, Point(W - 2 * L, 2 * L),
                        Color.GREEN,
                        { s ->
                            s.triangle(0f, 1f * L, -0.31f * L, 0.34f * L, 0.31f * L, 0.34f * L)
                            s.triangle(
                                    0.31f * L, 0.34f * L, 0.5f * L, -0.27f * L, 1f * L, 0.24f * L)
                            s.triangle(
                                    0.5f * L, -0.27f * L, 0.61f * L, -1f * L, 0f * L, -0.65f * L)
                            s.triangle(
                                    0f * L, -0.65f * L, -0.61f * L, -1f * L, -0.5f * L, -0.27f * L)
                            s.triangle(
                                    -0.5f * L, -0.27f * L, -1f * L, 0.24f * L, -0.31f * L, 0.34f * L)
                            s.triangle(
                                    -0.31f * L, 0.34f * L, 0f * L, -0.65f * L, 0.31f * L, 0.34f * L)
                            s.triangle(
                                    -0.31f * L, 0.34f * L, 0f * L, -0.65f * L, -0.5f * L, -0.27f * L)
                            s.triangle(
                                    0.31f * L, 0.34f * L, 0f * L, -0.65f * L, 0.5f * L, -0.27f * L)
                        }),
                Shape(ShapeType.TRIANGLE,
                        Point(W - 2 * L, H - 2 * L),
                        Color.RED,
                        { s -> s.triangle(-1f * L, -1f * L, 1f * L, -1f * L, 0f, 1f * L) }),
                Shape(ShapeType.RECT,
                        Point(2 * L, H - 2 * L),
                        Color.BLUE,
                        { s -> s.rect(-1f * L, -1f * L, 2f * L, 2f * L) }))

        for (shape in shapes) shapeRenderers.put(shape, ShapeRenderer())

        pe = ParticleEffect()
        pe.load(Gdx.files.internal("stars.party"), Gdx.files.internal(""))

        targetShape = shapes[(4 * Math.random()).toInt()]
        assetManager = AssetManager()
        assetManager.load("sound1.mp3", Sound::class.java)
        assetManager.load("sound2.mp3", Sound::class.java)
        assetManager.load("sound3.mp3", Sound::class.java)
        assetManager.load("sound4.mp3", Sound::class.java)
        assetManager.finishLoading()
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val tmpy = Gdx.graphics.height - 1f * Gdx.input.y
        val tmpx = 1f * Gdx.input.x

        if (Gdx.input.isTouched) {
            if (!dragging && isNear(currx, curry, tmpx, tmpy)) dragging = true

            if (dragging) {
                currx = tmpx
                curry = tmpy
            }
        } else {
            dragging = false
        }

        for (shape in shapes) {
            val shapeRenderer = shapeRenderers.get(shape)
            shapeRenderer!!.color = shape.color
            shapeRenderer!!.identity()
            shapeRenderer!!.translate(1f * shape.position.x, 1f * shape.position.y, 0f)
            shapeRenderer!!.begin(ShapeRenderer.ShapeType.Filled)
            shape.render(shapeRenderer)
            shapeRenderer!!.end()

            if (isNear(1f * shape.position.x, 1f * shape.position.y, currx, curry) &&
                    shape.shapeType == targetShape.shapeType && !expFlag) {
                dragging = false
                expFlag = true

                targetShape = shapes[(4 * Math.random()).toInt()]
                currx = W / 2f
                curry = H / 2f

                pe.reset()
                pe.getEmitters().first().setPosition(1f * shape.position.x, 1f * shape.position.y)
                pe.start()

                Gdx.input.vibrate(1000)

                val soundName = "sound" + (shape.shapeType.ordinal + 1) + ".mp3"
                val sound = assetManager.get(soundName, Sound::class.java)
                sound.play()
            }
        }

        if (!expFlag) {
            targetRenderer.color = Color.WHITE
            targetRenderer.begin(ShapeRenderer.ShapeType.Line)
            targetShape.render(targetRenderer)
            targetRenderer.end()

            targetRenderer.color = targetShape.color
            targetRenderer.identity()
            targetRenderer.translate(currx, curry, 0f)
            targetRenderer.begin(ShapeRenderer.ShapeType.Filled)
            targetShape.render(targetRenderer)
            targetRenderer.color = Color.WHITE
            targetRenderer.circle(0f, 0f, L/5f)
            targetRenderer.end()
        }

        renderExplosion()
    }

    private fun renderExplosion() {
        if (pe.isComplete()) {
            expFlag = false
            started = false
        }

        pe.update(Gdx.graphics.getDeltaTime())
        part.begin()
        pe.draw(part)
        part.end()
    }

    private fun isNear(x1: Float, y1: Float, x2: Float, y2: Float): Boolean =
            Math.pow(1.0 * (x1 - x2), 2.0) + Math.pow(1.0 * (y1 - y2), 2.0) < (4f * L * L / 9f)

    enum class ShapeType {
        CIRCLE, STAR, TRIANGLE, RECT
    }

    data class Shape(val shapeType: ShapeType,
                     val position: Point,
                     val color: Color,
                     val render: (ShapeRenderer) -> Unit)

    inner class CustomGestureDetector() : com.badlogic.gdx.input.GestureDetector.GestureListener {
        override fun zoom(initialDistance: Float, distance: Float): Boolean {
            return false
        }

        override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
            currx = W / 2f
            curry = H / 2f
            return false
        }

        override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
            return false
        }

        override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
            return false
        }

        override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
            return false
        }

        override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
            return false
        }

        override fun longPress(x: Float, y: Float): Boolean {
            return false
        }

        override fun pinch(initialPointer1: Vector2?,
                           initialPointer2: Vector2?,
                           pointer1: Vector2?,
                           pointer2: Vector2?): Boolean {
            return false
        }
    }
}
