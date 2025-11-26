import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class SnakePixels extends JPanel implements ActionListener, KeyListener {
    // Game area in pixels (grid cells)
    private final int GRID_COLS = 25;
    private final int GRID_ROWS = 18;
    // Size of one cell (the whole sprite area)
    private final int CELL = 32; // change to scale the whole game
    // size of a single "color pixel" inside a cell (we use 3x3)
    private final int PIX = CELL / 3;

    private final int WIDTH = GRID_COLS * CELL;
    private final int HEIGHT = GRID_ROWS * CELL;

    private ArrayList<Point> snake;
    private Point food;
    private int dir = KeyEvent.VK_RIGHT;
    private boolean running = false;
    private Timer timer;
    private int score = 0;
    private Random rand = new Random();

    public SnakePixels() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        initGame();
    }

    private void initGame() {
        snake = new ArrayList<>();
        // start in middle
        snake.add(new Point(GRID_COLS/2, GRID_ROWS/2));
        snake.add(new Point(GRID_COLS/2 - 1, GRID_ROWS/2));
        snake.add(new Point(GRID_COLS/2 - 2, GRID_ROWS/2));
        spawnFood();
        running = true;
        timer = new Timer(200, this); // adjust speed here
        timer.start();
    }

    private void spawnFood() {
        Point p;
        do {
            p = new Point(rand.nextInt(GRID_COLS), rand.nextInt(GRID_ROWS));
        } while (snake.contains(p));
        food = p;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!running) {
            drawGameOver(g);
            return;
        }

        // draw grid (optional, faint)
        g.setColor(new Color(30,30,30));
        for (int x = 0; x <= WIDTH; x += CELL) g.drawLine(x, 0, x, HEIGHT);
        for (int y = 0; y <= HEIGHT; y += CELL) g.drawLine(0, y, WIDTH, y);

        // draw food as a colorful 3x3 pixel sprite (red center with orange corners)
        drawPixelSprite(g, food.x, food.y, new Color[][] {
                { new Color(255,170,0), new Color(255,120,0), new Color(255,170,0) },
                { new Color(255,120,0), new Color(255,40,40),  new Color(255,120,0) },
                { new Color(255,170,0), new Color(255,120,0), new Color(255,170,0) }
        });

        // draw snake segments — head distinct, body alternating colors
        for (int i = 0; i < snake.size(); i++) {
            Point s = snake.get(i);
            if (i == 0) {
                // head: green with bright center
                drawPixelSprite(g, s.x, s.y, new Color[][] {
                        { new Color(40,160,40), new Color(60,200,60), new Color(40,160,40) },
                        { new Color(60,200,60), new Color(0,255,0),  new Color(60,200,60) },
                        { new Color(40,160,40), new Color(60,200,60), new Color(40,160,40) }
                });
            } else {
                // body: alternate between two greens for a pixel-y look
                if (i % 2 == 0) {
                    drawPixelSprite(g, s.x, s.y, new Color[][] {
                            { new Color(20,130,20), new Color(30,150,30), new Color(20,130,20) },
                            { new Color(30,150,30), new Color(20,180,20), new Color(30,150,30) },
                            { new Color(20,130,20), new Color(30,150,30), new Color(20,130,20) }
                    });
                } else {
                    drawPixelSprite(g, s.x, s.y, new Color[][] {
                            { new Color(15,90,15), new Color(25,110,25), new Color(15,90,15) },
                            { new Color(25,110,25), new Color(10,140,10), new Color(25,110,25) },
                            { new Color(15,90,15), new Color(25,110,25), new Color(15,90,15) }
                    });
                }
            }
        }

        // score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("Score: " + score, 8, 20);
    }

    // draw a 3x3 color array inside the grid cell (xGrid, yGrid)
    private void drawPixelSprite(Graphics g, int xGrid, int yGrid, Color[][] pixels) {
        int baseX = xGrid * CELL;
        int baseY = yGrid * CELL;
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                Color col = pixels[r][c];
                g.setColor(col);
                int px = baseX + c * PIX;
                int py = baseY + r * PIX;
                // draw a tiny border to emphasize "pixel" blocks
                g.fillRect(px, py, PIX, PIX);
                g.setColor(new Color(0,0,0,40)); // faint border
                g.drawRect(px, py, PIX-1, PIX-1);
            }
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 54));
        g.drawString("GAME OVER", WIDTH/2 - 200, HEIGHT/2 - 10);
        g.setFont(new Font("Monospaced", Font.PLAIN, 28));
        g.drawString("Score: " + score, WIDTH/2 - 60, HEIGHT/2 + 30);
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.drawString("Press R to restart", WIDTH/2 - 80, HEIGHT/2 + 70);
    }

    private void move() {
        Point head = snake.get(0);
        Point newHead = new Point(head);

        if (dir == KeyEvent.VK_UP) newHead.y--;
        if (dir == KeyEvent.VK_DOWN) newHead.y++;
        if (dir == KeyEvent.VK_LEFT) newHead.x--;
        if (dir == KeyEvent.VK_RIGHT) newHead.x++;

        // collisions with walls
        if (newHead.x < 0 || newHead.x >= GRID_COLS || newHead.y < 0 || newHead.y >= GRID_ROWS) {
            running = false;
            return;
        }
        // self collision
        if (snake.contains(newHead)) {
            running = false;
            return;
        }

        snake.add(0, newHead);

        if (newHead.equals(food)) {
            score += 10;
            // speed up slightly
            int d = Math.max(40, timer.getDelay() - 6);
            timer.setDelay(d);
            spawnFood();
        } else {
            snake.remove(snake.size()-1);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if ((k == KeyEvent.VK_LEFT) && dir != KeyEvent.VK_RIGHT) dir = KeyEvent.VK_LEFT;
        if ((k == KeyEvent.VK_RIGHT) && dir != KeyEvent.VK_LEFT) dir = KeyEvent.VK_RIGHT;
        if ((k == KeyEvent.VK_UP) && dir != KeyEvent.VK_DOWN) dir = KeyEvent.VK_UP;
        if ((k == KeyEvent.VK_DOWN) && dir != KeyEvent.VK_UP) dir = KeyEvent.VK_DOWN;

        if (!running && k == KeyEvent.VK_R) {
            // restart
            score = 0;
            dir = KeyEvent.VK_RIGHT;
            initGame();
            repaint();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake — Color Pixels");
            SnakePixels panel = new SnakePixels();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
