package Map;

import Engine.Config;
import Engine.Graphics;
import Game.Kirby;
import GameObject.Rectangle;
import Utils.Direction;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public abstract class Map {
    protected MapTile[] tiles;
    protected int width;
    protected int height;
    protected Tileset tileset;
    protected Camera camera;
    protected Point playerStartTile;
    protected int xMidPoint, yMidPoint;
    protected int startBoundX, startBoundY, endBoundX, endBoundY;
    private String mapFileName;

    public Map(String mapFileName, Tileset tileset, Rectangle screenBounds, Point playerStartTile) {
        this.mapFileName = mapFileName;
        this.tileset = tileset;
        loadMapFile();
        camera = new Camera(0, 0, screenBounds, tileset.getScaledSpriteWidth(), tileset.getScaledSpriteHeight(), this);
        this.startBoundX = 0;
        this.startBoundY = 0;
        this.endBoundX = width * tileset.getScaledSpriteWidth();
        this.endBoundY = height * tileset.getScaledSpriteHeight();
        this.xMidPoint = screenBounds.getWidth() / 2;
        this.yMidPoint = (screenBounds.getHeight() / 2);
        this.playerStartTile = playerStartTile;
    }

    private void loadMapFile() {
        Scanner fileInput;
        try {
            fileInput = new Scanner(new File(Config.MAP_FILES_PATH + this.mapFileName));
        } catch(FileNotFoundException ex) {
            System.out.println("Map file " + Config.MAP_FILES_PATH + this.mapFileName + " not found! Creating empty map file...");

            try {
                writeEmptyMapFile();
                fileInput = new Scanner(new File(Config.MAP_FILES_PATH + this.mapFileName));
            } catch(IOException ex2) {
                ex2.printStackTrace();
                System.out.println("Failed to create an empty map file!");
                throw new RuntimeException();
            }
        }

        this.width = fileInput.nextInt();
        this.height = fileInput.nextInt();
        this.tiles = new MapTile[this.height * this.width];
        fileInput.nextLine();

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int tileIndex = fileInput.nextInt();
                MapTile tile = tileset.getTile(tileIndex)
                        .build(j * tileset.getScaledSpriteWidth(), i * tileset.getScaledSpriteHeight());
                setTile(j, i, tile);
            }
        }

        fileInput.close();
    }

    private void writeEmptyMapFile() throws IOException {
        FileWriter fileWriter = null;
        fileWriter = new FileWriter(Config.MAP_FILES_PATH + this.mapFileName);
        fileWriter.write("0 0\n");
        fileWriter.close();
    }

    public MapTile[] getMapTiles() {
        return tiles;
    }

    public Rectangle getCamera() {
        return camera;
    }

    public MapTile getTile(int x, int y) {
        if (isInBounds(x, y)) {
            return tiles[x + width * y];
        } else {
            return null;
        }
    }

    public MapTile getTile(Point index) {
        if (isInBounds(index.x, index.y)) {
            return tiles[index.x + width * index.y];
        } else {
            return null;
        }
    }

    public void setTile(int x, int y, MapTile tile) {
        tiles[x + width * y] = tile;
    }

    public void setTile(Point index, MapTile tile) {
        tiles[index.x + width * index.y] = tile;
    }

    public int getTileWidth() {
        return tileset.getScaledSpriteWidth();
    }
    public int getTileHeight() {
        return tileset.getScaledSpriteHeight();
    }

    public MapTile getTileByPosition(int xPosition, int yPosition) {
        int xIndex = xPosition / Math.round(tileset.getSpriteWidth() * tileset.getTileScale());
        int yIndex = yPosition / Math.round(tileset.getSpriteHeight() * tileset.getTileScale());
        if (isInBounds(xIndex, yIndex)) {
            return getTile(xIndex, yIndex);
        } else {
            return null;
        }
    }

    public Point getTileIndexByPosition(int xPosition, int yPosition) {
        int xIndex = (xPosition + camera.getX()) / Math.round(tileset.getScaledSpriteWidth());
        int yIndex = (yPosition + camera.getY()) / Math.round(tileset.getScaledSpriteHeight());
        return new Point(xIndex, yIndex);
    }

    private boolean isInBounds(int x, int y) {
        int index = x + width * y;
        return x >= 0 && y >= 0 && x < width && y < height && index >= 0 && index < tiles.length;
    }

    public void update(Kirby player) {
        adjustMovementY(player);
        adjustMovementX(player);
        camera.update();
    }

    private void adjustMovementX(Kirby player) {
        int xMidPointDifference = 0;
        if (player.getX() > xMidPoint && camera.getEndBoundX() < endBoundX) {
            xMidPointDifference = xMidPoint - player.getX();
            player.moveX(xMidPointDifference);
            camera.moveX(-xMidPointDifference);
            if (camera.getEndBoundX() > endBoundX) {
                int cameraDifference = camera.getEndBoundX() - endBoundX;
                player.moveX(cameraDifference);
                camera.moveX(-cameraDifference);
            }
        } else if (player.getX() < xMidPoint && camera.getX() > startBoundX) {
            xMidPointDifference = xMidPoint - player.getX();
            player.moveX(xMidPointDifference);
            camera.moveX(-xMidPointDifference);
            if (camera.getX() < startBoundX) {
                int cameraDifference = startBoundX - camera.getX();
                player.moveX(-cameraDifference);
                camera.moveX(cameraDifference);
            }
        }
    }

    private void adjustMovementY(Player player) {
        int yMidPointDifference = 0;
        if (player.getY() > yMidPoint && camera.getEndBoundY() < endBoundY) {
            yMidPointDifference = yMidPoint - player.getY();
            player.moveY(yMidPointDifference);
            camera.moveY(-yMidPointDifference);
            if (camera.getEndBoundY() > endBoundY) {
                int cameraDifference = camera.getEndBoundY() - endBoundY;
                player.moveY(cameraDifference);
                camera.moveY(-cameraDifference);
            }
        } else if (player.getY() < yMidPoint && camera.getY() > startBoundY) {
            yMidPointDifference = yMidPoint - player.getY();
            player.moveY(yMidPointDifference);
            camera.moveY(-yMidPointDifference);
            if (camera.getY() < startBoundY) {
                int cameraDifference = startBoundY - camera.getY();
                player.moveY(-cameraDifference);
                camera.moveY(cameraDifference);
            }
        }
    }

    public void draw(Graphics graphics) {
        camera.draw(graphics);
    }

    public Point getPlayerStartPosition() {
        MapTile tile = getTile(playerStartTile.x, playerStartTile.y);
        return new Point(tile.getX(), tile.getY());
    }

    public Tileset getTileset() {
        return tileset;
    }

    public int getWidthPixels() {
        return width * tileset.getScaledSpriteWidth();
    }

    public int getHeightPixels() {
        return height * tileset.getScaledSpriteHeight();
    }

    public String getMapFileName() {
        return mapFileName;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int newWidth, Direction direction) {
        if (direction == Direction.RIGHT || direction == Direction.LEFT) {
            int oldWidth = this.width;
            MapTile[] mapTilesSizeChange = new MapTile[this.height * newWidth];

            if (direction == Direction.RIGHT) {
                for (int i = 0; i < this.height; i++) {
                    for (int j = 0; j < oldWidth; j++) {
                        if (j < newWidth) {
                            mapTilesSizeChange[j + newWidth * i] = tiles[j + oldWidth * i];
                        }
                    }
                }
            } else /* if (direction == Direction.LEFT) */ {
                int difference = newWidth - oldWidth;
                for (int i = 0; i < this.height; i++) {
                    for (int j = oldWidth - 1; j >= 0; j--) {
                        if (j + difference >= 0) {
                            MapTile tile = tiles[j + oldWidth * i];
                            mapTilesSizeChange[j + difference + newWidth * i] = tile;
                            tile.moveX(tileset.getScaledSpriteWidth() * difference);
                        }
                    }
                }
            }

            this.tiles = mapTilesSizeChange;

            for (int i = 0; i < this.height; i++) {
                for (int j = 0; j < newWidth; j++) {
                    if (tiles[j + newWidth * i] == null) {
                        tiles[j + newWidth * i] = tileset.getDefaultTile()
                                .build(j * tileset.getScaledSpriteWidth(), i * tileset.getScaledSpriteHeight());
                    }
                }
            }

            this.width = newWidth;
        }
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int newHeight, Direction direction) {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            int oldHeight = this.height;
            MapTile[] mapTilesSizeChange = new MapTile[newHeight * this.width];

            if (direction == Direction.DOWN) {
                for (int i = 0; i < oldHeight; i++) {
                    if (i < newHeight) {
                        for (int j = 0; j < this.width; j++) {
                            mapTilesSizeChange[j + this.width * i] = tiles[j + this.width * i];
                        }
                    }
                }
            } else /* if (direction == Direction.UP) */ {
                int difference = newHeight - oldHeight;
                for (int i = oldHeight - 1; i >= 0; i--) {
                    if (i + difference >= 0) {
                        for (int j = 0; j < this.width; j++) {

                            MapTile tile = tiles[j + this.width * i];
                            mapTilesSizeChange[j + this.width * (i + difference)] = tile;
                            tile.moveY(tileset.getScaledSpriteHeight() * difference);
                        }
                    }
                }
            }

            this.tiles = mapTilesSizeChange;

            for (int i = 0; i < newHeight; i++) {
                for (int j = 0; j < this.width; j++) {
                    if (tiles[j + this.width * i] == null) {
                        tiles[j + this.width * i] = tileset.getDefaultTile()
                                .build(j * tileset.getScaledSpriteWidth(), i * tileset.getScaledSpriteHeight());
                    }
                }
            }

            this.height = newHeight;
        }
    }

}
