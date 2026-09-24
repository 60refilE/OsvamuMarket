package py.edu.fpuna.dto;

public class MensajeClienteStock {
    private int pagina;

    public MensajeClienteStock(int pagina) {
        this.pagina = pagina;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }
}