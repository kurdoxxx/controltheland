package gui;

import com.btxtech.game.jsre.client.common.Constants;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.gameengine.services.collision.ForceField;
import com.btxtech.game.jsre.common.gameengine.services.collision.impl.NoBetterPathFoundException;
import com.btxtech.game.jsre.common.gameengine.services.terrain.SurfaceType;
import com.btxtech.game.jsre.common.gameengine.services.terrain.Terrain;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainTile;
import com.btxtech.game.jsre.common.gameengine.services.terrain.TerrainUtil;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import model.MovingModel;
import scenario.Scenario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: beat
 * Date: 10.04.13
 * Time: 15:07
 */
public abstract class AbstractGui {
    private static final int FRAMES_PER_SECOND = 25;
    private static final long TIMER_DELAY = 1000 / FRAMES_PER_SECOND;
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private JPanel canvas;
    private JComboBox<Scenario> scenarioBox;
    private MovingModel movingModel;
    private VOMonitor voMonitor;

    public void setMovingModel(MovingModel movingModel) {
        this.movingModel = movingModel;
    }

    public MovingModel getMovingModel() {
        return movingModel;
    }

    public AbstractGui() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Razarion Collision Demo");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.setPreferredSize(new Dimension(800, 800));
                addComponentsToPane(frame.getContentPane());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    protected abstract void addToMenuBar(JPanel menu);

    protected abstract void customDraw(Graphics graphics);

    protected abstract void onScenarioChanged(Scenario scenario) throws NoBetterPathFoundException;

    public void setScenarios(final java.util.List<Scenario> scenarios) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    scenarioBox.setModel(new DefaultComboBoxModel<Scenario>(scenarios.toArray(new Scenario[scenarios.size()])));
                    onScenarioChanged((Scenario) scenarioBox.getSelectedItem());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void addComponentsToPane(Container pane) {
        if (!(pane.getLayout() instanceof BorderLayout)) {
            pane.add(new JLabel("Container doesn't use BorderLayout!"));
            return;
        }

        // Setup menu
        JPanel menu = new JPanel();
        menu.setLayout(new FlowLayout());
        pane.add(menu, BorderLayout.PAGE_START);

        final JLabel mouseLabel = new JLabel("Mouse Position:");
        menu.add(mouseLabel);

        final JLabel updateLabel = new JLabel("---");
        menu.add(updateLabel);

        addToMenuBar(menu);

        // Setup canvas
        canvas = new JPanel() {
            @Override
            public void paint(Graphics graphics) {
                super.paint(graphics);
                updateLabel.setText("Last Update: " + TIME_FORMAT.format(System.currentTimeMillis()));
                drawTerrain(graphics);
                drawForceField(graphics);
                drawGridTerrain(graphics);
                try {
                    customDraw(graphics);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        canvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseLabel.setText("Mouse Position: " + e.getX() + ":" + e.getY());
            }
        });

        // Scenarios
        scenarioBox = new JComboBox<Scenario>();
        scenarioBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    onScenarioChanged((Scenario) scenarioBox.getSelectedItem());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        menu.add(scenarioBox);
        // Item selection
        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SyncItem syncItem = getMovingModel().getItemAtPosition(new Index(e.getX(), e.getY()));
                if(syncItem != null) {
                    startMonitor(syncItem);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        canvas.setBackground(Color.WHITE);
        pane.add(new JScrollPane(canvas), BorderLayout.CENTER);
    }

    public void startMonitor(SyncItem syncItem) {
        stopMonitor();
        voMonitor = new VOMonitor(syncItem, getMovingModel().getCollisionService());
    }

    public void stopMonitor() {
        if(voMonitor != null) {
            voMonitor.close();
        }
    }

    private void drawTerrain(Graphics graphics) {
        if (movingModel == null || movingModel.getTerrain() == null) {
            return;
        }
        graphics.setColor(new Color(0, 0, 0));
        Terrain terrain = movingModel.getTerrain();
        for (int x = 0; x < graphics.getClipBounds().getWidth(); x += Constants.TERRAIN_TILE_WIDTH) {
            for (int y = 0; y < graphics.getClipBounds().getHeight(); y += Constants.TERRAIN_TILE_HEIGHT) {
                TerrainTile terrainTile = terrain.getTerrainTile(TerrainUtil.getTerrainTileIndexForAbsXPosition(x), TerrainUtil.getTerrainTileIndexForAbsYPosition(y));
                if (terrainTile.getSurfaceType() == SurfaceType.BLOCKED) {
                    graphics.fillRect(x, y, Constants.TERRAIN_TILE_WIDTH, Constants.TERRAIN_TILE_HEIGHT);
                }
            }
        }
    }

    public void start() {
        Timer timer = new Timer("GuiTimerTask", true);
        timer.schedule(new GuiTimerTask(), 0, TIMER_DELAY);
    }

    class GuiTimerTask extends TimerTask {
        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (canvas != null) {
                        canvas.repaint();
                    }
                }
            });
        }
    }

    private void drawGridTerrain(Graphics graphics) {
        graphics.setColor(Color.DARK_GRAY);
        for (int i = Constants.TERRAIN_TILE_WIDTH; i < graphics.getClipBounds().getWidth(); i += Constants.TERRAIN_TILE_WIDTH) {
            graphics.drawLine(i, 0, i, (int) graphics.getClipBounds().getHeight());
        }
        for (int i = Constants.TERRAIN_TILE_HEIGHT; i < graphics.getClipBounds().getHeight(); i += Constants.TERRAIN_TILE_HEIGHT) {
            graphics.drawLine(0, i, (int) graphics.getClipBounds().getWidth(), i);
        }
    }

    private void drawGridCollision(Graphics graphics) {
        graphics.setColor(Color.LIGHT_GRAY);
        for (int i = Constants.COLLISION_TILE_WIDTH; i < graphics.getClipBounds().getWidth(); i += Constants.COLLISION_TILE_WIDTH) {
            graphics.drawLine(i, 0, i, (int) graphics.getClipBounds().getHeight());
        }
        for (int i = Constants.COLLISION_TILE_HEIGHT; i < graphics.getClipBounds().getHeight(); i += Constants.COLLISION_TILE_HEIGHT) {
            graphics.drawLine(0, i, (int) graphics.getClipBounds().getWidth(), i);
        }

    }

    private void drawForceField(Graphics graphics) {
        graphics.setColor(Color.LIGHT_GRAY);
        for (int i = ForceField.FIELD_SIZE; i < graphics.getClipBounds().getWidth(); i += ForceField.FIELD_SIZE) {
            graphics.drawLine(i, 0, i, (int) graphics.getClipBounds().getHeight());
        }
        for (int i = ForceField.FIELD_SIZE; i < graphics.getClipBounds().getHeight(); i += ForceField.FIELD_SIZE) {
            graphics.drawLine(0, i, (int) graphics.getClipBounds().getWidth(), i);
        }
        /*ForceField forceField = movingModel.getForceField();
        for (int x = 0; x < forceField.getXCount(); x++) {
            for (int y = 0; y < forceField.getYCount(); y++) {
                double angel = forceField.getAngel(x, y);
                Index middle = new Index(x * ForceField.FIELD_SIZE + ForceField.FIELD_SIZE / 2, y * ForceField.FIELD_SIZE + ForceField.FIELD_SIZE / 2);
                Index end = middle.getPointFromAngelToNord(angel, ForceField.FIELD_SIZE / 2);
                graphics.setColor(Color.RED);
                graphics.drawLine(middle.getX(), middle.getY(), end.getX(), end.getY());
                Index arrow = end.getPointFromAngelToNord(angel + MathHelper.HALF_RADIANT, 2);
                graphics.setColor(Color.BLACK);
                graphics.drawLine(end.getX(), end.getY(), arrow.getX(), arrow.getY());
            }
        }*/
    }

    protected void drawSyncItem(final Graphics graphics, MovingModel movingModel) {
        movingModel.iterateOverSyncItems(new MovingModel.SyncItemCallback() {
            @Override
            public void onSyncItem(SyncItem syncItem) {
                graphics.setColor(getSyncItemColor(syncItem));
                graphics.drawArc(syncItem.getPosition().getX() - syncItem.getRadius(),
                        syncItem.getPosition().getY() - syncItem.getRadius(),
                        syncItem.getDiameter(),
                        syncItem.getDiameter(),
                        0, 360);

                //Index angel = syncItem.getPosition().getPointFromAngelToNord(syncItem.getAngel(), syncItem.getRadius() + 10);
                //graphics.setColor(Color.GREEN);
                //graphics.drawLine(syncItem.getPosition().getX(),
                //        syncItem.getPosition().getY(),
                //        angel.getX(),
                //        angel.getY());
                graphics.setColor(getSyncItemColor(syncItem));
                graphics.drawString(Integer.toString(syncItem.getId()),
                        syncItem.getPosition().getX() - 5,
                        syncItem.getPosition().getY() + 5);
            }
        });
    }

    private Color getSyncItemColor(SyncItem syncItem) {
        if (syncItem.isMoving()) {
            return Color.BLUE;
        } else {
            return Color.GREEN;
        }
    }

}