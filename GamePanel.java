import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter; 
import java.util.Scanner;
import java.io.IOException;
import java.lang.Math;

public class GamePanel extends JPanel {
    public static final int FRAME = 400;
    public static final Color BACKGROUND = new Color(255, 255, 255);

    private Graphics myBuffer;
    private BufferedImage myImage;

    private BufferedImage regular, run1, run2, deadimage;  // Dino images
    private int distance1, distance2;

    public static int movex = -4;
    private Timer t;

    private Dino dino;
        private boolean space, dead;
        private int jumpX;

    private Cactus cactus, cactus2, cactus3;
        private boolean isCactus, isCactus2, isCactus3, multipleCactus;
        private int counter, cactus2Counter, cactus3Counter;

    private Fireball fireball;
        private boolean isFireball;

    private String highScore;
        private String newHighScore;;

    private int[][] locations = new int[4][];
    private int[] dinoloc = new int[2];
    private int[] cactus1loc = new int[2];
    private int[] cactus2loc = new int[2];
    private int[] cactus3loc = new int[2];

    public GamePanel() throws IOException {
        myImage = new BufferedImage(FRAME, FRAME, BufferedImage.TYPE_INT_RGB);
        myBuffer = myImage.getGraphics();
        myBuffer.setColor(BACKGROUND);
        myBuffer.fillRect(0, 0, FRAME, FRAME);
    
        isCactus = false;
        isCactus2 = false;
        isCactus3 = false;
        counter = 0;

        dinoloc[0] = 0;
        dinoloc[1] = 0;
        
        cactus1loc[0] = 0;
        cactus1loc[1] = 0;
        
        cactus2loc[0] = 0;
        cactus2loc[1] = 0;
        
        cactus3loc[0] = 0;
        cactus3loc[1] = 0;
        
        locations[0] = dinoloc;
        locations[1] = cactus1loc;
        locations[2] = cactus2loc;
        locations[3] = cactus3loc;


        regular   = ImageIO.read(new File( "images/DinoRegular.png"));
        run1      = ImageIO.read(new File( "images/DinoRun1.png"   ));
        run2      = ImageIO.read(new File( "images/DinoRun2.png"   ));
        deadimage = ImageIO.read(new File( "images/DinoDead.png"   ));

        fireball = new Fireball(99999, 99999);

        t = new Timer(5, new AnimationListener());
        space = false;
        addKeyListener(new Key());
        setFocusable(true);

        // Read high score
        readScore();
 } // public GamePanel()

    public void begin() {
        t.start();
    } // Start timer

    // Animation
    public void animate() throws IOException {
        myBuffer.setColor(BACKGROUND);
        myBuffer.fillRect(0, 0, FRAME, FRAME);
        dead = dino.dead();
        dinoloc[0] = dino.getX();
        dinoloc[1] = dino.getY();

        // Jump
        if (space && !dead) {
            int newY = dino.jumpEquation(jumpX);
            dino.setY((FRAME - 50) - newY - dino.getHeight());
            jumpX++;
        }

        if (jumpX - 1 != 0 && dino.jumpEquation(jumpX - 1) == 0) space = false;

        // -------- Spawn Cactuses --------
        int randInt = getRandomInt(100);
        
        if (randInt < 10 && !isCactus) {
            cactus = randomCactus();
            cactus.setMoveX(movex);
            isCactus = true;
        }

        if (randInt < 3 && !isCactus2) {
            cactus2 = randomCactus();
            cactus2.setMoveX(movex);
            isCactus2 = true;

            distance1 = getRandomInt(20);
            if (distance1 < 10) distance1 = 10;
            cactus2Counter = counter + distance1;
        }

        if(randInt < 1 && !isCactus3) {
            cactus3 = randomCactus();
            cactus3.setMoveX(movex);
            isCactus3 = true;

            distance2 = getRandomInt(20);
            if (distance2 < 10) distance2 = 10;
            cactus3Counter = cactus2Counter + distance2;
        }

        // Fireball
        if(isFireball && !dead) {
            fireball.moveX();

            for(int n = 1; n <= 3; n++) {
                if( fireball.getX() + Fireball.width >= dino.getclx(n) &&
                    fireball.getY() + Fireball.height >= dino.getcty(n) ) {
                    isFireball = false;
                    if(n==1) { isCactus = false; }
                    else if(n==2) { isCactus2 = false; }
                    else { isCactus3 = false; }
                    break;
                } 
            }
            if(isFireball) fireball.draw(myBuffer);
        }

        // Draw cactuses
        multipleCactus = false;
        if(counter > 100) multipleCactus = true;
    
        if (isCactus) {
            cactus.draw(myBuffer);
            cactus1loc[0] = cactus.getX();
            cactus1loc[1] = cactus.getY();
            if (!dead) cactus.moveX();
        }
        if (isCactus2) {
            cactus2loc[0] = cactus2.getX();
            cactus2loc[1] = cactus2.getY();
        }
        if (isCactus3) {
            cactus3loc[0] = cactus3.getX();
            cactus3loc[1] = cactus3.getY();
        }
        if (multipleCactus) {
            if (isCactus2 && counter > cactus2Counter ) {
                cactus2.draw(myBuffer);
                if (!dead) cactus2.moveX();
            }
            if (isCactus3 && counter > cactus3Counter) {
                cactus3.draw(myBuffer);
                if (!dead) cactus3.moveX();
            }
        }
        // Check if cactuses are off the screen
        if (isCactus  && cactus.getX()  < -200) isCactus  = false;
        if (isCactus2 && cactus2.getX() < -500) isCactus2 = false;
        if (isCactus3 && cactus3.getX() < -800) isCactus3 = false;
        if (isFireball && fireball.getX() > FRAME) isFireball = false;

        // Send cactus data to dino to calculate if dino is dead
        if(isCactus)  sendCactusData(1, cactus);
        if(isCactus2) sendCactusData(2, cactus2);
        if(isCactus3) sendCactusData(3, cactus3);
        
        // Animate legs
        if (counter % 10 < 5) dino.setImage(run1);
        else dino.setImage(run2);

        if (dino.aboveGround()) dino.setImage(regular);
        if (dead) {
            dino.setImage(deadimage);
        }
        dino.draw(myBuffer);

        // Text
        myBuffer.setFont(new Font("Monospaced", Font.BOLD, 14));
        myBuffer.setColor(new Color(200, 200, 200));
        myBuffer.drawString("HI " + highScore + " | " +
                            Integer.toString(counter / 5), FRAME - 120, 50);

        if (!dead) counter++;
        repaint();

    } // public void animate()

    private class AnimationListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                animate();
            } catch (IOException e4) {
                e4.printStackTrace();
            }
        }
    }

    private class Key extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if ((e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_UP) && !space) {
                space = true;
                jumpX = 0;
            }
            
            if(e.getKeyCode() == 39 && !isFireball && dino.getType().equals("DinoFire")) {
                fireball = new Fireball( 
                    dino.getdrx() + 2, 
                    dino.getY() + (dino.getdby() - dino.getdty()) / 2 - Fireball.height / 2
                );
                fireball.draw(myBuffer);
                isFireball = true;
            }
        } // public void keyPressed
    } // private class Key

    public void reset(char type) throws IOException {
        if (type == 's') dino = new DinoSuper(); 
        else if (type == 'f') dino = new DinoFire(); 
        else dino = new DinoRegular();

        myBuffer.setColor(BACKGROUND);
        myBuffer.fillRect(0, 0, FRAME, FRAME);
        dino.draw(myBuffer);

        if (isCactus)  cactus.setX(-99999);
        if (isCactus2) cactus2.setX(-99999);
        if (isCactus3) cactus3.setX(-99999);

        newHighScore = Integer.toString(counter/5);
        saveScore();
        readScore();
    
        dead = dino.dead();
        counter = 0;
        isFireball = false;

        repaint();
    } // public void reset()

    public void paintComponent(Graphics g) {
        g.drawImage(myImage, 0, 0, getWidth(), getHeight(), null);
    }

    public int getRandomInt(int max) {
        return (int) (Math.random() * max);
    }
    
    public void sendCactusData(int n, Cactus cactus) {
        dino.setclx(n, cactus.getX());
        dino.setcrx(n, cactus.getX() + cactus.getWidth());
        dino.setcty(n, cactus.getY());
        dino.setcby(n, cactus.getY() + cactus.getHeight());
    }
 
    public Cactus randomCactus() throws IOException {
        int x = getRandomInt(4);
        if (x == 0)      return new CactusGroup();
        else if (x == 1) return new CactusRegular1();
        else if (x == 2) return new CactusSmall1();
        else if (x == 3) return new CactusSmall2();
        else             return new CactusSmall3();
    }
    private void readScore() throws IOException {
        File file = new File("highScore.txt");
        Scanner reader = new Scanner(file);
        highScore = reader.nextLine();
        reader.close();
    }
    private void saveScore() throws IOException {
        if(Integer.parseInt(newHighScore) > Integer.parseInt(highScore)) {
            FileWriter writer = new FileWriter("highscore.txt");
            writer.write(newHighScore);
            writer.close();
        }
    
    }
} // public class GamePanel extends JPanel

