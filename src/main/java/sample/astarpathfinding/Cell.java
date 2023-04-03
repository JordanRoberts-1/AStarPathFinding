package sample.astarpathfinding;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Cell extends Rectangle {

    public static final int CELL_SIZE = 13;

    private boolean isAvailable;

    private int xIndex;
    private int yIndex;

    private int gCost;
    private int hCost;

    private Cell parent;

    public Cell(int x, int y) {
        super(CELL_SIZE, CELL_SIZE, Color.WHITE);
        xIndex = x;
        yIndex = y;
        isAvailable = true;
    }

    public void setAvailable(boolean x){
        isAvailable = x;
        setFill(isAvailable ? Color.WHITE : Color.BLACK);
    }

    public int getxIndex() {
        return xIndex;
    }

    public int getyIndex() {
        return yIndex;
    }

    public void setgCost(int gCost) {
        this.gCost = gCost;
    }

    public void sethCost(int hCost) {
        this.hCost = hCost;
    }

    public int fCost(){
        return gCost + hCost;
    }

    public int getgCost() {
        return gCost;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setParent(Cell parent) {
        this.parent = parent;
    }

    public Cell getParentCell(){
        if(parent != null){
            return parent;
        }
        return null;
    }
}
