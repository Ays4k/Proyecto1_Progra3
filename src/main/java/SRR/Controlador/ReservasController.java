package SRR.Controlador;

import SRR.DTO.CategoriaDTO;
import SRR.Servicio.CategoriaServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;


public class ReservasController {

    @FXML DatePicker date;

    private CategoriaServicio categoriaServicio = new CategoriaServicio();

    private ObservableList<CategoriaDTO> listaCategorias;

    @FXML private ListView<CategoriaDTO> listCategoria;
    @FXML private Button btnReservar;
    @FXML private ComboBox<LocalTime> cmbInicio;
    @FXML private ComboBox<LocalTime> cmbFinal;

    @FXML
    public void initialize() {

        date.setDayCellFactory(seleccion -> new DateCell(){
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if(item != null && item.isBefore(LocalDate.now())){
                    setDisable(true);
                    setStyle("-fx-background-color: #ffc0cb;");
                }
            }
        });

        cargarCategorias();
        btnReservar.setOnAction(event -> {
            System.out.println(listCategoria.getSelectionModel().getSelectedItems());
        });

        for(LocalTime hora = LocalTime.of(8,0); !hora.isAfter(LocalTime.of(16,30)); hora = hora.plusMinutes(30)){
            cmbInicio.getItems().add(hora);
        }

        cmbInicio.getSelectionModel().selectedItemProperty().addListener((event, viejo, nuevo) ->{
            cmbFinal.getItems().clear();
            for(LocalTime hora = nuevo.plusMinutes(30); !hora.isAfter(LocalTime.of(17,00)); hora = hora.plusMinutes(30)){
                cmbFinal.getItems().add(hora);
            }
        });
    }

    private void cargarCategorias() {
        listaCategorias = FXCollections.observableArrayList(categoriaServicio.obtenerCategorias());
        listCategoria.setCellFactory(listView -> {
            ListCell<CategoriaDTO> cell  = new ListCell<>() {
                @Override
                protected void updateItem(CategoriaDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDescripcion());
                    }
                }
            };

            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                listCategoria.requestFocus();
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    // Alternar la selección del elemento cliqueado sin borrar los demás
                    if (listCategoria.getSelectionModel().getSelectedIndices().contains(index)) {
                        listCategoria.getSelectionModel().clearSelection(index);
                    } else {
                        listCategoria.getSelectionModel().select(index);
                    }
                    // Consumir el evento para evitar que JavaFX ejecute el comportamiento por defecto (que borra la selección anterior)
                    event.consume();
                }
            });

            return cell;

        });



        listCategoria.setItems(listaCategorias);
        listCategoria.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);



        //Explicacion:
        // listaCategoria es un observable list (si se le agrega algo se actualiza en el listview)
        //listCategoria.setCellFactory le dice como va a construir sus celdas, solo queremos mostrar la descripcion de la categoria
        //listCategoria.setItems le dice que items va a mostrar, en este caso la listaCategorias
        //listCategoria.getSelectionModel().setSelectionMode le dice que modo de seleccion va a tener, en este caso MULTIPLE
        //cuando se se use .getSelectionModel().getSelectedItems() se va a obtener una lista de categorias seleccionadas
        //se envia al servicio de hacer reservas
    }
}