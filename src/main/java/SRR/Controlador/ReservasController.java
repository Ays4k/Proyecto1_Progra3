package SRR.Controlador;

import SRR.DTO.CategoriaDTO;
import SRR.DTO.ReservaAiDTO;
import SRR.DTO.ReservaDTO;
import SRR.Servicio.CategoriaServicio;
import SRR.Servicio.ReservaServicio;
import SRR.Utilidades.Sesion;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.security.spec.ECField;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalTime;


public class ReservasController {

    ReservaServicio servicio = new ReservaServicio();

    @FXML DatePicker date;

    private CategoriaServicio categoriaServicio = new CategoriaServicio();

    private ObservableList<CategoriaDTO> listaCategorias;

    private ObservableList<ReservaDTO> resList;
    @FXML private TableView<ReservaDTO> tableRes;
    @FXML private TableColumn<ReservaDTO,String> idColum;
    @FXML private TableColumn<ReservaDTO,String> actColum;
    @FXML private TableColumn<ReservaDTO,String> fchColum;
    @FXML private TableColumn<ReservaDTO,String> horColum;
    @FXML private TableColumn<ReservaDTO,String> recColum;
    @FXML private TableColumn<ReservaDTO,String> estColum;


    @FXML private ListView<CategoriaDTO> listCategoria;


    @FXML private Button btnAi;
    @FXML private Label lblAi;
    @FXML private Button btnReservar;
    @FXML private Button btnCan;
    @FXML private Button btnClear;

    @FXML private TextField txtAi;

    @FXML private ComboBox<LocalTime> cmbInicio;
    @FXML private ComboBox<LocalTime> cmbFinal;
    @FXML private TextField txtActividad;

    @FXML
    public void initialize() {
        tableRes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColum.setCellValueFactory(new PropertyValueFactory<>("id"));
        actColum.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        fchColum.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        horColum.setCellValueFactory(new PropertyValueFactory<>("id"));
        recColum.setCellValueFactory(new PropertyValueFactory<>("idsRecursos"));
        estColum.setCellValueFactory(new PropertyValueFactory<>("estado"));
        try{
            resList = FXCollections.observableList(servicio.reservasActivasDe(Sesion.getId()));
        }catch (Exception e){
            e.printStackTrace();
        }
        tableRes.setItems(resList);

        lblAi.setText("");

        /*
        tableRes.getSelectionModel().selectedItemProperty().addListener((obj, oldv, newv)->{
            txtActividad.setText(obj.getValue().getActividad());
            cmbInicio.getItems().clear();
            cmbInicio.setValue(LocalTime.parse(obj.getValue().getHoraInicio()));
            cmbFinal.getItems().clear();
            cmbFinal.setValue(LocalTime.parse(obj.getValue().getHoraFin()));
            date.setValue(LocalDate.parse(obj.getValue().getFecha()));
        });
        nota: podria llenarse los campos si se selecciona
        */
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

        btnAi.setOnAction(event -> reservaAi());
        btnReservar.setOnAction(event -> {
            reservar();
            limpiar();
        });
        btnCan.setOnAction(event -> {
            cancelar();
        });
        btnClear.setOnAction(event -> {
            limpiar();
        });

        cmbInicio.getSelectionModel().selectedItemProperty().addListener((event, viejo, nuevo) ->{
            cmbFinal.getItems().clear();
            for(LocalTime hora = nuevo.plusMinutes(30); !hora.isAfter(LocalTime.of(17,0)); hora = hora.plusMinutes(30)){
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
    private void reservaAi() {
        if(txtAi.getText().isEmpty()){
            return;
        }
        String prompt = txtAi.getText().trim();
        lblAi.setText("Cargando...");
        lblAi.setStyle("-fx-text-fill: orange");
        txtAi.setDisable(true);
        txtActividad.setDisable(true);
        date.setDisable(true);
        cmbInicio.setDisable(true);
        cmbFinal.setDisable(true);
        listCategoria.setDisable(true);
        Thread peticion = new Thread(() ->{
            try {
                ReservaAiDTO res = servicio.generarReservaAi(prompt);
                Platform.runLater(()->{
                    txtActividad.setText(res.getActividad());
                    date.setValue(res.getFecha());
                    cmbInicio.setValue(res.getHoraInicio());
                    cmbFinal.setValue(res.getHoraFinal());
                    for(String categoria : res.getCategorias()){
                        for(int i = 0; i<listCategoria.getItems().size(); i++){
                            if(listCategoria.getItems().get(i).getDescripcion().equals(categoria)){
                                listCategoria.getSelectionModel().select(i);
                                System.out.println(1);
                                break;
                            }
                        }
                    }
                    listCategoria.requestFocus();
                    lblAi.setText("Infomación extraída con exito");
                    lblAi.setStyle("-fx-text-fill: green");
                    txtAi.setDisable(false);
                    txtActividad.setDisable(false);
                    date.setDisable(false);
                    cmbInicio.setDisable(false);
                    cmbFinal.setDisable(false);
                    listCategoria.setDisable(false);
                    txtAi.setText("");
                });

            }catch (Exception e){
                Alert alerta = new Alert(Alert.AlertType.ERROR);
                alerta.setHeaderText("Error con la IA");
                alerta.setContentText("Error: " + e.getMessage());
                alerta.showAndWait();
                lblAi.setText("Intentelo Nuevamente");
                lblAi.setStyle("-fx-text-fill: red");
                txtAi.setDisable(false);
                txtActividad.setDisable(false);
                date.setDisable(false);
                cmbInicio.setDisable(false);
                cmbFinal.setDisable(false);
                listCategoria.setDisable(false);
                txtAi.setText("");
            }
        });

        peticion.start();

    }
    private void cancelar(){
        if(tableRes.getSelectionModel().getSelectedItem() == null){
            return;
        }
        ReservaDTO res = tableRes.getSelectionModel().getSelectedItem();
        resList.remove(res);
        servicio.cancelarReserva(res.getId());
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

        // crearReserva ahora recibe las categorias y devuelve la reserva ya armada,
        // con el id generado y el recurso libre de cada categoria asignado
        ReservaDTO reserva;
        try {
            reserva = servicio.crearReserva(Sesion.getId(), actividad, fecha, horaInicio, horaFinal, categorias);
            resList.add(reserva);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    private void limpiar(){
        txtActividad.clear();
        date.setValue(null);
        cmbInicio.getItems().clear();
        cmbFinal.getItems().clear();
        listCategoria.getSelectionModel().clearSelection();
    }
}