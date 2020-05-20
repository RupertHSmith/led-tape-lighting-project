package database;

public interface IUpdateDatabase {
    void writeDeviceState(boolean standby, int intensity);
}
