package models;

public class AllocationPlante {
    private int id;
    private int id_plante;
    private int id_terrain;
    private String date_allocation;

    public AllocationPlante(int id, int id_plante, int id_terrain, String date_allocation) {
        this.id = id;
        this.id_plante = id_plante;
        this.id_terrain = id_terrain;
        this.date_allocation = date_allocation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_plante() {
        return id_plante;
    }

    public void setId_plante(int id_plante) {
        this.id_plante = id_plante;
    }

    public int getId_terrain() {
        return id_terrain;
    }

    public void setId_terrain(int id_terrain) {
        this.id_terrain = id_terrain;
    }

    public String getDate_allocation() {
        return date_allocation;
    }

    public void setDate_allocation(String date_allocation) {
        this.date_allocation = date_allocation;
    }

    @Override
    public String toString() {
        return "AllocationPlante{" +
                "id=" + id +
                ", id_plante=" + id_plante +
                ", id_terrain=" + id_terrain +
                ", date_allocation='" + date_allocation + '\'' +
                '}';
    }
}
