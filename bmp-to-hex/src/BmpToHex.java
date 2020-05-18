import java.io.*;

public class BmpToHex {
    private String imagePath;
    private String outputFile;

    public static void main (String[] args){
        BmpToHex bmpToHex = new BmpToHex("chars//csvs//", "output.txt");
        bmpToHex.makeFile();
    }

    public BmpToHex(String imageFolder, String output){
        this.imagePath = imageFolder;
        this.outputFile = output;
    }

    public void makeFile(){
        try {
            String output = "{ ";
            for (int i = 0; i < 10; i++){
                String charFile = String.format("%schar_%d.csv", imagePath, i);
                File file = new File(charFile);
                if (file.isFile()){

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String csvLine = bufferedReader.readLine();
                        boolean[][] bitmap = parseCSVLine(csvLine, 128,96);
                        String hexString = convertToHex(bitmap, 128, 96);
                        bufferedReader.close();
                        output += hexString;

                } else {
                    System.err.println("File not found");
                }
            }

            output = output.substring(0, output.length() - 2);
            output += "}; ";
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            out.println(output);
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }



    }

    private String convertToHex(boolean[][] bitmap, int height, int width){
        String output = "";
        for (int w = 0; w < width; w++) {
            int noBytes = height / 8;
            for (int nByte = 0; nByte < noBytes; nByte++) {
                int bitArray = 0;
                for (int x = 0; x < 8; x++) {
                    if (bitmap[w][(nByte * 8) + x]) {
                        bitArray |= 1 << (7 - x);
                    }

                }
                //now convert bit array to hex and output to string
                String hexString = Integer.toHexString(bitArray);
                hexString = String.format("0x%1$2s", hexString).replace(' ','0').toUpperCase();
                System.out.println("HEX: " + hexString);
                output += hexString + ", ";
            }
        }

        return output;
    }


    private boolean[][] parseCSVLine(String csvLine, int height, int width){
        String[] bits = csvLine.split(",");
        boolean[][] bitmap = new boolean[width][height];

        if (bits.length != height * width) {
            System.err.println("ERROR NO PARSE");
            return null;
        }

        int counter = 0;
        for (int h = 0; h < height; h++){
            for (int w = 0; w < width; w++){
                bitmap[w][h] = bits[counter].trim().equals("1");
                counter++;
            }
        }
        return bitmap;
    }
}
