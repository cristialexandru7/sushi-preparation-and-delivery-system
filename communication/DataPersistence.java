package communication;

import java.io.*;

public class DataPersistence implements Runnable {

    Server server;

    public DataPersistence(Server server) {
        this.server = server;
    }

    public void saveState() {
        File bck1 = new File("database.bck.new");
        File bck2 = new File("stock.bck.new");
        ObjectOutputStream objectOutputStream;
        try (FileOutputStream fileOutputStream = new FileOutputStream("database.bck.new", false)) {
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            synchronized (server.getDatabase()) {
                objectOutputStream.writeObject(server.getDatabase());
            }

            objectOutputStream.flush();
            objectOutputStream.close();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream("stock.bck.new", false)) {
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            synchronized (server.getStockManagement()) {
                objectOutputStream.writeObject(server.getStockManagement());
            }

            objectOutputStream.flush();
            objectOutputStream.close();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        File old1 = new File("database.bck");
        File old2 = new File("stock.bck");
        bck1.renameTo(old1);
        bck2.renameTo(old2);
    }

    public void loadState() {
        File bck1 = new File("database.bck");
        File bck2 = new File("stock.bck");
        ObjectInputStream objectInputStream;
        try (FileInputStream fileInputStream = new FileInputStream("database.bck")) {
            objectInputStream = new ObjectInputStream(fileInputStream);
            Object object = objectInputStream.readObject();
            if(object instanceof Database)
                server.setDatabase((Database) object);

            objectInputStream.close();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }

        try (FileInputStream fileInputStream = new FileInputStream("stock.bck")) {
            objectInputStream = new ObjectInputStream(fileInputStream);
            Object object = objectInputStream.readObject();
            if(object instanceof StockManagement)
                server.setStockManagement((StockManagement) object);

            objectInputStream.close();

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }

    @Override
    public void run() {
        while(true) {
            this.saveState();
        }
    }
}
