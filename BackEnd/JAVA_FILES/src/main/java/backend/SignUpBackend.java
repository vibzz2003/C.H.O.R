package backend;

import java.io.*;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import javax.crypto.Cipher;  
import javax.crypto.SecretKey;  
import javax.crypto.SecretKeyFactory;  
import javax.crypto.spec.IvParameterSpec;  
import javax.crypto.spec.PBEKeySpec;  
import javax.crypto.spec.SecretKeySpec;  
import java.nio.charset.StandardCharsets;  
import java.security.InvalidAlgorithmParameterException;  
import java.security.InvalidKeyException;  
import java.security.NoSuchAlgorithmException;  
import java.security.spec.InvalidKeySpecException;  
import java.security.spec.KeySpec;  
import java.util.Base64;  
import javax.crypto.BadPaddingException;  
import javax.crypto.IllegalBlockSizeException;  
import javax.crypto.NoSuchPaddingException;  

public class SignUpBackend {
	
	static String Encrypt(String Password) throws CsvValidationException, IOException, NoSuchAlgorithmException {
		
		String secret_key,salt_val;
		CSVReader reader = new CSVReader(new FileReader("Param.csv"));
		String[] nextLine;
		nextLine = reader.readNext();
		secret_key = nextLine[0];
		salt_val = nextLine[1];
		
	    try   
	    {  
	      /* Declare a byte array. */  
	      byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};  
	      IvParameterSpec ivspec = new IvParameterSpec(iv);
	      
	      /* Create factory for secret keys. */  
	      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");  
	      
	      /* PBEKeySpec class implements KeySpec interface. */  
	      KeySpec spec = new PBEKeySpec(secret_key.toCharArray(), salt_val.getBytes(), 65536, 256);  
	      SecretKey tmp = factory.generateSecret(spec);  
	      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");  
	      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");  
	      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);  
	      
	      /* Returns encrypted value. */  
	      return Base64.getEncoder().encodeToString(cipher.doFinal(Password.getBytes(StandardCharsets.UTF_8)));  
	    }   
	    catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e)   
	    {  
	      System.out.println("Error occured during encryption: " + e.toString());  
	    } 
	    
	    return null;
	}
	
	public Document addUser(String Name, String UserName, String PhoneNo, String Password) throws CsvValidationException, NoSuchAlgorithmException, IOException {
		
		MongoClient mc = MongoClients.create("mongodb+srv://Aditya07:Adit%405207902@cluster0.v2wojna.mongodb.net/?retryWrites=true&w=majority");
		MongoDatabase db = mc.getDatabase("USERS_INFO");
		MongoCollection<Document> col = db.getCollection("USERS");
		Bson filters = Filters.and(Filters.eq("UserName",UserName));
		FindIterable<Document> fr = col.find(filters);
		Iterator<Document> it = fr.iterator();	
		if(it.hasNext()){
			Document res = new Document("ResCode",409);
			res.append("Msg","User already exists!");
			return res;
		}
		
		Document user = new Document("UserName",UserName);
		user.append("Name",Name);
		user.append("Phone_Number",PhoneNo);
		Password = Encrypt(Password);
		user.append("Password",Password);
		
		if(col.insertOne(user) != null){
			Document res = new Document("ResCode",200);
			res.append("Msg","User Created!");
			return res;
		}
		Document res = new Document("ResCode",202);
		res.append("Msg","User not created");
		
		return res;
		
	}

	public static void main(String[] args) throws CsvValidationException, NoSuchAlgorithmException, IOException {

		SignUpBackend user = new SignUpBackend();	
		System.out.println(SignUpBackend.Encrypt("AdityaSharma"));
		System.out.println(user.addUser("Aditya Sharma","devganaditya@gmail.com","7428025402","Adit@5207902"));	
		
	}

}