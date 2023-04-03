module sample.astarpathfinding {
    requires javafx.controls;
    requires javafx.fxml;


    opens sample.astarpathfinding to javafx.fxml;
    exports sample.astarpathfinding;
}