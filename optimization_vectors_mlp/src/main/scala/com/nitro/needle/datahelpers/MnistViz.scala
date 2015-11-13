package com.nitro.needle.datahelpers

import java.awt._
import java.awt.event.{KeyEvent, WindowEvent}
import java.awt.geom.{AffineTransform, Point2D}
import java.awt.image.{AffineTransformOp, BufferedImage, WritableRaster}
import javax.swing._

/**
 * MNIST Swing-based visualization
 *
 * @author Marek Kolodziej
 *
 * @param figTitle
 * @param features
 * @param targets
 * @param predicted
 */
class MnistViz(
                val figTitle: String,
                val features: Seq[Array[Double]],
                val targets: Seq[Int],
                val predicted: Option[Seq[Int]] = None)
  extends JFrame {

  setTitle(figTitle)
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

  val rows = math.round(math.sqrt(features.size)).toInt
  val cols = (math.ceil(features.size.toDouble / rows)).toInt
  val lay = new GridLayout(rows, cols)
  this.
    setLayout(lay)

  for (idx <- 0 until features.size) {

    val single = features(idx)
    val label = targets(idx)
    val pixels = single.map(i => 255 - i / (single.max - single.min) * 255)
    val wd = math.round(math.sqrt(pixels.size)).toInt
    val ht = math.round(pixels.size.toDouble / wd).toInt

    val image = new BufferedImage(wd, ht, BufferedImage.TYPE_BYTE_GRAY)
    val raster = image.getData.asInstanceOf[WritableRaster]
    raster.setPixels(0, 0, wd, ht, pixels)
    image.setData(raster)

    val resizedImg = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY)

    val g = resizedImg.createGraphics()
    val at = AffineTransform.getTranslateInstance(0, 0)
    at.scale(2.0, 2.0)
    at.rotate(Math.toRadians(90), 50, 50)
    val trans = MnistViz.findTranslation(at, image)
    at.preConcatenate(trans)

    val op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
    val newImg = op.filter(image, null)

    g.drawImage(newImg, 100, 0, -100, 100, null)
    g.dispose()

    val text = targets(idx).toString
    val jl = new JLabel(text, SwingConstants.CENTER)
    jl.setToolTipText(text)
    jl.setText(text)
    jl.setHorizontalTextPosition(SwingConstants.CENTER)
    jl.setVerticalTextPosition(SwingConstants.TOP)
    val ii = new ImageIcon(resizedImg)
    jl.setIcon(ii)
    add(jl)
    jl.setHorizontalAlignment(SwingConstants.CENTER)
    jl.setVerticalAlignment(SwingConstants.CENTER)
  }

  val thisFrame = this

  KeyboardFocusManager.
    getCurrentKeyboardFocusManager
    .addKeyEventDispatcher(new KeyEventDispatcher() {
      override def dispatchKeyEvent(e: KeyEvent): Boolean = {
        if (e.getKeyCode == KeyEvent.VK_ESCAPE) {
          dispatchEvent(new WindowEvent(thisFrame, WindowEvent.WINDOW_CLOSING))
        }
        false
      }
    })

  val dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize()
  val xx = ((dimension.getWidth() - getWidth()) / 2).toInt
  val yy = ((dimension.getHeight() - getHeight()) / 2).toInt
  setLocation(xx, yy)

  setExtendedState(Frame.MAXIMIZED_BOTH)
  setFocusable(true)
  setAlwaysOnTop(true)
  setVisible(true)
  setAlwaysOnTop(false)
}

/**
 * MNIST Swing-based visualization companion object
 *
 * @author Marek Kolodziej
 */
object MnistViz {

  def findTranslation(at: AffineTransform, bi: BufferedImage): AffineTransform = {

    val p2dInY = new Point2D.Double(0.0, 0.0)
    val p2dOutY = at.transform(p2dInY, null)
    val yTrans = p2dOutY.getY

    val p2dInX = new Point2D.Double(0.0, bi.getHeight)
    val p2dOutX = at.transform(p2dInX, null)
    val xTrans = p2dOutX.getX

    val at2 = new AffineTransform()
    at2.translate(-xTrans, -yTrans)
    at2
  }


}