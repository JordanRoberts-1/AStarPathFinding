package sample.astarpathfinding;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;

public class Controller {

    @FXML private TilePane tilePane;
    @FXML private TextField startingXTextField;
    @FXML private TextField startingYTextField;
    @FXML private TextField endingXTextField;
    @FXML private TextField endingYTextField;
    @FXML private Slider updateDelaySlider;

    AStar aStar;

    private static final int numCellsX = 100;
    private static final int numCellsY = 60;

    private Cell[][] cells = new Cell[numCellsX][numCellsY];

    public void initialize(){

        //Set prompt text for the user input texts
        startingXTextField.setPromptText("Numbers 1-100");
        startingYTextField.setPromptText("Numbers 1-60");
        endingXTextField.setPromptText("Numbers 1-100");
        endingYTextField.setPromptText("Numbers 1-60");

        //Set the min and max of the update delay option
        updateDelaySlider.setMin(2.0);
        updateDelaySlider.setMax(300.0);

        //fill the tilePane with all of the cells
        for (int i = 0; i < numCellsY; i++) {
            for(int j = 0; j < numCellsX; j++){
                cells[j][i] = new Cell(j, i);
                //add each cell to the tile pane
                tilePane.getChildren().add(cells[j][i]);
            }
        }

        //Set Drawing capabilities to the tilePane
        tilePane.setOnMouseDragged(e -> {
            if (e.getPickResult().getIntersectedNode().getClass() == Cell.class) {
                Cell c = (Cell)e.getPickResult().getIntersectedNode();
                if(e.isPrimaryButtonDown()){
                    c.setAvailable(false);
                } else {
                    c.setAvailable(true);
                }
            }
        });

        //allow user to not only drag but also click to change the availability of the cell
        tilePane.setOnMouseClicked(e -> {
            if(e.getPickResult().getIntersectedNode().getClass() == Cell.class){
                Cell c = (Cell)e.getPickResult().getIntersectedNode();
                if(e.getButton() == MouseButton.PRIMARY){
                    c.setAvailable(false);
                } else {
                    c.setAvailable(true);
                }
            }
        });
    }

    @FXML
    public void clearBoard(){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[0].length; j++) {
                Cell c = cells[i][j];
                if(c.isAvailable()){
                    c.setFill(Color.WHITE);
                }
            }
        }

        //if there is alread a astar going, stop it
        if(aStar != null){
            aStar.stop();
        }
    }

    @FXML
    public void calculate(){
        //Clear the board
        clearBoard();

        //store the starting and ending position as chosen by the user
        int startingX;
        int startingY;
        int endingX;
        int endingY;
        try {
            startingX = Integer.parseInt(startingXTextField.getText()) - 1;
            startingY = Integer.parseInt(startingYTextField.getText()) - 1;
            endingX = Integer.parseInt(endingXTextField.getText()) - 1;
            endingY = Integer.parseInt(endingYTextField.getText()) - 1;
        } catch (Exception e) {
            showErrorPopup();
            return;
        }

        //Store the starting and ending Cell's based on the chosen position
        Cell startingCell = cells[startingX][startingY];
        Cell endingCell = cells[endingX][endingY];

        //create a new instance of A* to run
        aStar = new AStar(startingCell, endingCell);

        //The main loop that loops through the algorithm and updates the cells on screen
        new AnimationTimer(){

            private double lastUpdate;
            private double timer = 0;
            @Override
            public void start(){
                lastUpdate = System.nanoTime();
                super.start();
            }

            @Override
            public void handle(long now) {
                double elapsedSeconds = (now - lastUpdate) / 1_000_000_000.0;
                timer += elapsedSeconds;

                //if the algorithm is still going, and the timer is above the user's chosen delay,
                // then do another loop of the algorithm
                if(!aStar.pathFound() && timer > (1/updateDelaySlider.getValue())){
                    aStar.setPathFound(aStar.runOneLoop());
                    timer = 0;
                }

                //if a path was found, stop this animationTimer
                if (aStar.pathFound()) {
                    stop();
                }

                lastUpdate = now;
            }
        }.start();
    }

    private void showErrorPopup() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        HBox dialogHbox = new HBox();
        Label text = new Label("You must input numbers for the positions!");
        dialogHbox.getChildren().add(text);
        dialogHbox.setAlignment(Pos.CENTER);
        Scene dialogScene = new Scene(dialogHbox, 300, 100);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    public class AStar {
        //The list of all open cells
        //an open cell is a potential candidate to calculate next
        private ArrayList<Cell> open = new ArrayList<>();

        //the Set of all closed cells
        //a closed cell is a cell that has already been evaluated
        private HashSet<Cell> closed = new HashSet<>();

        private Cell targetCell;
        private boolean pathFound = false;
        private double secondsTook;

        public AStar(Cell startingCell, Cell endingCell) {
            targetCell = endingCell;

            //add the starting cell to the open list of cells
            open.add(startingCell);
        }

        //function to auto run the program if desired
        public void autoRun(){
            while (!pathFound) {
                pathFound = runOneLoop();
            }
        }

        //this is the main body of the algorithm, the other parts loop this one so that the user can choose
        //how the loop itself runs
        public boolean runOneLoop(){
            long startingNano = System.nanoTime();

            //if there are no more cells to evaluate then end the loop
            if (open.isEmpty()) {
                return true;
            }

            //find the lowest fCost of all potential cells in the open list of cells
            //and assign the found cell to current
            int currentFCost = Integer.MAX_VALUE;
            Cell current = null;
            for (Cell c : open) {
                if(c.fCost() <= currentFCost){
                    currentFCost = c.fCost();
                    current = c;
                }
            }

            //remove current from the open set and add it to the closed set, while updating it's color
            open.remove(current);
            closed.add(current);
            current.setFill(Color.RED);

            //if current is the target, then print the path and return true
            if (current == targetCell) {
                retracePath(current);
                return true;
            }

            /* This block looks at all the candidate neighbor cells of the current cell and evaluates their costs and stuff
            *           /   /   /
            *           /   O   /
            *           /   /   /
            * */
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    //if i & j point to the current cell, then just skip it
                    if (i == 0 && j == 0) {
                        continue;
                    }

                    //store the current X and current Y value that we are evaluating
                    int currentX = current.getxIndex() + i;
                    int currentY = current.getyIndex() + j;

                    //Limit the x and y to where there are actually cells
                    if (currentX < 0 || currentX > numCellsX - 1 || currentY < 0 || currentY > numCellsY - 1) {
                        continue;
                    }

                    //The current neighbor cell that we are evaluating
                    //if the neighbor is not available (a wall) or in the closed set, then continue
                    Cell currentCalc = cells[currentX][currentY];
                    if (!currentCalc.isAvailable() || closed.contains(currentCalc)) {
                        continue;
                    }

                    //figure out the movement cost (fCost) of going to this neighbor
                    int movementCostToNeighbor = current.getgCost() + calcDistance(current, currentCalc);

                    //if the cost of moving to the neighbor is less than staying here and the current neighbor is not
                    //in the open list, then add it to the path
                    if (movementCostToNeighbor < currentCalc.getgCost() || !open.contains(currentCalc)) {
                        currentCalc.setgCost(movementCostToNeighbor);
                        currentCalc.sethCost(calcDistance(targetCell, currentCalc));
                        currentCalc.setParent(current);

                        if (!open.contains(currentCalc)) {
                            open.add(currentCalc);
                            currentCalc.setFill(Color.GREEN);
                        }
                    }
                }
            }
            long endingNano = System.nanoTime();
            secondsTook += (endingNano - startingNano)/1_000_000_000.0;
            return false;
        }

        //retraces the created path by starting and the ending node and working it's way back
        private void retracePath(Cell current) {
            while (current.getParentCell() != null) {
                current.setFill(Color.YELLOW);
                current = current.getParentCell();
            }
            System.out.println("Finished by finding a path. It took " + secondsTook + " seconds for calculating");
        }

        //calculates the distance between two cells
        private int calcDistance(Cell a, Cell b) {
            int xDist = Math.abs(b.getxIndex() - a.getxIndex());
            int yDist = Math.abs(b.getyIndex() - a.getyIndex());

            if (xDist > yDist) {
                return 14 * yDist + 10 * (xDist - yDist);
            }
            return 14 * xDist + 10 * (yDist - xDist);
        }

        public void stop(){
            setPathFound(true);
        }

        public boolean pathFound(){
            return pathFound;
        }

        public void setPathFound(boolean x){
            pathFound = x;
        }
    }
}
