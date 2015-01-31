package com.josepmtomas.rockgame.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.josepmtomas.rockgame.GameActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.opengl.GLES30.*;
import static android.opengl.GLUtils.texImage2D;
import static org.apache.http.util.EntityUtils.toByteArray;

/**
 * Created by Josep on 17/07/2014.
 * @author Josep
 */

@SuppressWarnings("unused")
public class TextureHelper
{
	private static final String TAG = "TextureHelper";

	// PKM HEADER BYTES
	// 4 - Magic number: "PKM "
	// 2 - Byte version
	// 2 - Byte data type
	// 2 - Extended width (big endian)
	// 2 - Extended height (big endian)
	// 2 - Original width (big endian)
	// 2 - Original height (big endian)

	private static final int PKM_HEADER_SIZE = 16;
	private static final int PKM_HEADER_WIDTH_OFFSET = 12;
	private static final int PKM_HEADER_HEIGHT_OFFSET = 14;


	// Loading a cube map into OpenGL
	public static int loadCubeMap(Context context, int[] cubeResources)
	{
		final int[] textureObjectIds = new int[1];

		glGenTextures(1, textureObjectIds, 0);

		if(textureObjectIds[0] == 0)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Could not generate a new OpenGL texture object");
			}
			return 0;
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		final Bitmap[] cubeBitmaps = new Bitmap[6];

		for(int i=0; i<6; i++)
		{
			cubeBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), cubeResources[i], options);

			if(cubeBitmaps[i] == null)
			{
				if(LoggerConfig.ON)
				{
					Log.w(TAG, "Resource ID " + cubeResources[i] + "could not be decoded.");
				}
				glDeleteTextures(1, textureObjectIds, 0);
				return 0;
			}
		}

		glBindTexture(GL_TEXTURE_CUBE_MAP, textureObjectIds[0]);

		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0);

		glBindTexture(GL_TEXTURE_2D, 0);

		for(Bitmap bitmap : cubeBitmaps)
		{
			bitmap.recycle();
		}

		return textureObjectIds[0];
	}


	public static int loadCubeMap(Context context, String[] fileNames)
	{
		final int[] textureObjectIds = new int[1];

		glGenTextures(1, textureObjectIds, 0);

		if(textureObjectIds[0] == 0)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Could not generate a new OpenGL texture object");
			}
			return 0;
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;
		final Bitmap[] cubeBitmaps = new Bitmap[6];

		AssetManager assetManager = context.getAssets();
		InputStream inputStream;

		for(int i=0; i<6; i++)
		{
			//cubeBitmaps[i] = BitmapFactory.decodeResource(context.getResources(), cubeResources[i], options);
			try
			{
				inputStream = assetManager.open(fileNames[i]);
				cubeBitmaps[i] = BitmapFactory.decodeStream(inputStream);

				if(cubeBitmaps[i] == null)
				{
					if(LoggerConfig.ON)
					{
						Log.w(TAG, "File '" + fileNames[i] + "' could not be decoded.");
					}
					glDeleteTextures(1, textureObjectIds, 0);
					return 0;
				}
				else
				{
					inputStream.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		glBindTexture(GL_TEXTURE_CUBE_MAP, textureObjectIds[0]);

		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, cubeBitmaps[0], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, cubeBitmaps[1], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, cubeBitmaps[2], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, cubeBitmaps[3], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, cubeBitmaps[4], 0);
		texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, cubeBitmaps[5], 0);

		glBindTexture(GL_TEXTURE_2D, 0);

		for(Bitmap bitmap : cubeBitmaps)
		{
			bitmap.recycle();
		}

		return textureObjectIds[0];
	}


	/**
	 *
	 * @param context application context
	 * @param resourceId resource identifier
	 * @return reference to a OpenGL texture
	 */
	public static int loadTexture(Context context, int resourceId)
	{
		final int[] textureObjectIds = new int[1];
		glGenTextures(1, textureObjectIds, 0);

		if(textureObjectIds[0] == 0)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Could not generate a new OpenGL texture object.");
			}
			return 0;
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inScaled = false;

		final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

		if(bitmap == null)
		{
			if(LoggerConfig.ON)
			{
				Log.w(TAG, "Resource ID " + resourceId + " could not be decoded.");
			}

			glDeleteTextures(1, textureObjectIds, 0);
			return 0;
		}

		glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

		bitmap.recycle();

		glGenerateMipmap(GL_TEXTURE_2D);

		glBindTexture(GL_TEXTURE_2D, 0);

		return textureObjectIds[0];
	}


	@SuppressWarnings("all")
	public static int loadETC2Texture(Context context, String[] filenames, int compression, Boolean bClamp, Boolean bHighQuality)
	{
		int textures[] = new int[1];
		glGenTextures(1, textures, 0);

		int textureID = textures[0];
		glBindTexture(GL_TEXTURE_2D, textureID);

		if(bHighQuality)
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		}
		else
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST_MIPMAP_NEAREST);
		}

		if(bClamp)
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		}
		else
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		}

		try
		{
			for(int i=0; i<filenames.length; i++)
			{
				//Log.d(TAG, "Loading mip level " + i);
				InputStream is = context.getResources().getAssets().open(filenames[i]);
				int fileSize = (int) context.getResources().getAssets().openFd(filenames[i]).getLength();
				byte[] data = new byte[fileSize];
				is.read(data);

				ByteBuffer buffer = ByteBuffer.allocateDirect(data.length)
						.order(ByteOrder.LITTLE_ENDIAN)
						.put(data);
				buffer.position(PKM_HEADER_SIZE);

				ByteBuffer header = ByteBuffer.allocateDirect(PKM_HEADER_SIZE)
						.order(ByteOrder.BIG_ENDIAN)
						.put(data, 0, PKM_HEADER_SIZE);
				header.position(0);

				int width = header.getShort(PKM_HEADER_WIDTH_OFFSET);
				int height = header.getShort(PKM_HEADER_HEIGHT_OFFSET);

				glCompressedTexImage2D(GL_TEXTURE_2D, i, compression, width, height, 0, data.length - PKM_HEADER_SIZE, buffer);

				is.close();
			}
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, filenames.length-1);

			glBindTexture(GL_TEXTURE_2D, 0);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return textureID;
	}


	@SuppressWarnings("all")
	public static int loadETC2Texture(Context context, String fileName, int compression, Boolean bClamp, Boolean bHighQuality)
	{
		int textures[] = new int[1];
		glGenTextures(1, textures, 0);

		int textureID = textures[0];
		glBindTexture(GL_TEXTURE_2D, textureID);

		if(bHighQuality)
		{
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		}
		else
		{
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}

		if(bClamp)
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		}
		else
		{
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		}

		try
		{
			InputStream is =  context.getResources().getAssets().open(fileName);
			int fileSize = (int)context.getResources().getAssets().openFd(fileName).getLength();
			byte[] data = new byte[fileSize];
			is.read(data);

			ByteBuffer buffer = ByteBuffer.allocateDirect(data.length)
					.order(ByteOrder.LITTLE_ENDIAN)
					.put(data);
			buffer.position(PKM_HEADER_SIZE);

			ByteBuffer header = ByteBuffer.allocateDirect(PKM_HEADER_SIZE)
					.order(ByteOrder.BIG_ENDIAN)
					.put(data, 0, PKM_HEADER_SIZE);
			header.position(0);

			int width = header.getShort(PKM_HEADER_WIDTH_OFFSET);
			int height = header.getShort(PKM_HEADER_HEIGHT_OFFSET);

			glCompressedTexImage2D(GL_TEXTURE_2D, 0, compression, width, height, 0, data.length - PKM_HEADER_SIZE, buffer);
			/*glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 4);
			glGenerateMipmap(GL_TEXTURE_2D);*/

			glBindTexture(GL_TEXTURE_2D, 0);

			is.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return textureID;
	}
}
