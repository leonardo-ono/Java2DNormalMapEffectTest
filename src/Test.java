

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 2D Normal Map Effect
 * 
 * reference: https://en.wikipedia.org/wiki/Normal_mapping
 * 
 * resources:
 * normal_map2.png - https://stackoverflow.com/questions/9345708/incorrect-lighting-in-normal-maps
 * normal_map3.jpg - https://br.pinterest.com/pin/11892386506573491/
 * normal_map5.jpg - https://beta.friendlyshade.com/normalizer
 * 
 * @author Leo
 */
public class Test extends JPanel implements MouseMotionListener {
    
    private BufferedImage normalMap;
    private BufferedImage off;
    
    private Vec3 mousePosition = new Vec3(0, 0, -10);
    
    public Test() {
        try {
            normalMap = ImageIO.read(getClass().getResourceAsStream("normal_map5.jpg"));
            //normalMap = ImageIO.read(getClass().getResourceAsStream("normal_map2.png"));
            off = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        addMouseMotionListener(this);
    }
    
    private final Vec3 normal = new Vec3();
    private final Vec3 lightDirection = new Vec3();
    private final Vec3 normalMapPosition = new Vec3();
    
    private void generateOff(Vec3 mousePos) {
        off.getGraphics().clearRect(0, 0, off.getWidth(), off.getHeight());
        for (int y = 0; y < normalMap.getHeight(); y++) {
            for (int x = 0; x < normalMap.getWidth(); x++) {
                int mc = normalMap.getRGB(x, y);
                
                int b = mc & 0xff;
                int g = (mc >> 8) & 0xff;
                int r = (mc >> 16) & 0xff;
                
                // ref: https://en.wikipedia.org/wiki/Normal_mapping
                //  X: -1 to +1 :  Red:     0 to 255
                //  Y: -1 to +1 :  Green:   0 to 255
                //  Z:  0 to -1 :  Blue:  128 to 255
                normal.x = (r - 128) / 128.0;
                normal.y = (128 - g) / 128.0; // y is inverted because it's using screen space
                normal.z = (128 - b) / 128.0;
                normal.normalize();
                
                normalMapPosition.set(x, y, 0);
                lightDirection.set(mousePos);
                lightDirection.sub(normalMapPosition);
                
                lightDirection.normalize();
                
                double intensity = normal.dot(lightDirection);
                
                if (intensity < 0.05) {
                    intensity = 0.05;
                }
                
                int color = (int) (255 * intensity);
                int c = (color << 16) + (color << 8) + color;
                off.setRGB(x, y, c);
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        generateOff(mousePosition);        
        
        g.drawImage(off, 0, 0, null);
        
        g.setColor(Color.RED);
        g.fillOval((int) (mousePosition.x - 4), (int) (mousePosition.y - 4), 8, 8);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Test test = new Test();
            test.setPreferredSize(new Dimension(800, 600));
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setTitle("Java 2D Normal Map Test");
            frame.add(test);
            frame.pack();
            frame.setResizable(false);
            //frame.setLocationRelativeTo(null);
            frame.setLocation(50, 50);
            frame.setVisible(true);
            test.requestFocus();
        });
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePosition.set(e.getX(), e.getY(), -100);
        repaint();
    }
    
}
