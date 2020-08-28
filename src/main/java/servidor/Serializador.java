package servidor;

import licitador.Licitador;

import java.io.*;
import java.util.ArrayList;

public class Serializador {
    public static void serialize(Object toSerialize, String fileName) throws IOException {
        FileOutputStream file = new FileOutputStream(fileName);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(toSerialize);
        out.close();
        file.close();
    }
    public static Object deserialize (String fileName){
        Object deserialized = null;
        try {
            FileInputStream file = new FileInputStream(fileName);
            ObjectInputStream in = new ObjectInputStream(file);
            deserialized = in.readObject();
            in.close();
            file.close();
        } catch (FileNotFoundException e) {
            System.out.println("O ficheiro " + fileName + " não existe");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return deserialized;
    }

}
