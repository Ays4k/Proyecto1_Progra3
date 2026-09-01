package SRR.Controlador;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.ReservaDTO;
import SRR.Servicio.CategoriaServicio;
import SRR.Servicio.ReservaServicio;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.ArrayList;
import java.awt.TextField;
import java.time.LocalDate;
import java.time.LocalTime;
import javafx.scene.control.ComboBox;



public class ReservasController {

    ReservaServicio servicio = new ReservaServicio();

    @FXML DatePicker date;

    private CategoriaServicio categoriaServicio = new CategoriaServicio();

    private ObservableList<CategoriaDTO> listaCategorias;

    @FXML private ListView<CategoriaDTO> listCategoria;
    @FXML private Button btnReservar;
    @FXML private ComboBox<LocalTime> cmbInicio;
    @FXML private ComboBox<LocalTime> cmbFinal;
    @FXML private TextField txtActividad;

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
        date.valueProperty().addListener((evento,oldvalue,newvalue)->{

            int horaActual;
            int minutos;
            if(newvalue.equals(LocalDate.now())){
                horaActual = LocalTime.now().getMinute()<30 ? LocalTime.now().getHour() : LocalTime.now().getHour() + 1;
                minutos = LocalTime.now().getMinute()<30 ? 30 : 0;
            }
            else{
                horaActual = 8;
                minutos = 0;
            }
            for(LocalTime hora = LocalTime.of(horaActual,minutos); !hora.isAfter(LocalTime.of(16,30)); hora = hora.plusMinutes(30)){
                cmbInicio.getItems().add(hora);
            }
        });

        cargarCategorias();
        btnReservar.setOnAction(event -> {
            System.out.println(listCategoria.getSelectionModel().getSelectedItems());
        });



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
        //por ultimo, a la factory le agregamos un event filtrer, para que cuando seleccionamos una celda no deseleccione las demas
    }

    private void reservar(){
        String actividad = txtActividad.getText().trim();
        String fecha = date.valueProperty().get().toString();
        String horaInicio = cmbInicio.getSelectionModel().getSelectedItem().toString();
        String horaFinal = cmbFinal.getSelectionModel().getSelectedItem().toString();
        List<String> categorias = new ArrayList<String>();
        for(CategoriaDTO cat : listCategoria.getSelectionModel().getSelectedItems()){
            categorias.add(cat.getId());
        }

        ReservaDTO res = new ReservaDTO("1","111",categorias,actividad,fecha,horaInicio,horaFinal,"Pendiente");

        servicio.crearReserva(res);


    }
}