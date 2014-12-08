package com.josepmtomas.rockgame.objects;

import android.content.Context;
import android.content.res.Resources;

import com.josepmtomas.rockgame.Constants;
import com.josepmtomas.rockgame.algebra.vec3;
import com.josepmtomas.rockgame.collision.CollisionCylinder;
import com.josepmtomas.rockgame.collision.CollisionSphere;
import com.josepmtomas.rockgame.data.IndexBuffer;
import com.josepmtomas.rockgame.data.VertexBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.*;
import static android.opengl.GLES30.*;

/**
 * Created by Josep on 16/07/2014.
 */
public class StaticMesh
{
	private static final String TAG = "StaticMesh";

	protected static final int POSITION_COMPONENTS = 3;
	protected static final int TEXCOORD_COMPONENTS = 2;
	protected static final int NORMAL_COMPONENTS = 3;
	protected static final int TANGENT_COMPONENTS = 4;
	protected static final int COLOR_COMPONENTS = 3;
	protected static final int TOTAL_COMPONENTS =
			POSITION_COMPONENTS + TEXCOORD_COMPONENTS + NORMAL_COMPONENTS + TANGENT_COMPONENTS + COLOR_COMPONENTS;

	protected static final int POSITION_OFFSET = 0;
	protected static final int TEXCOORD_OFFSET = POSITION_OFFSET + POSITION_COMPONENTS;
	protected static final int NORMAL_OFFSET = TEXCOORD_OFFSET + TEXCOORD_COMPONENTS;
	protected static final int TANGENT_OFFSET = NORMAL_OFFSET + NORMAL_COMPONENTS;
	protected static final int COLOR_OFFSET = TANGENT_OFFSET + TANGENT_COMPONENTS;

	protected static final int POSITION_BYTE_OFFSET = 0;
	protected static final int TEXCOORD_BYTE_OFFSET = POSITION_BYTE_OFFSET + (POSITION_COMPONENTS * Constants.BYTES_PER_FLOAT);
	protected static final int NORMAL_BYTE_OFFSET = TEXCOORD_BYTE_OFFSET + (TEXCOORD_COMPONENTS * Constants.BYTES_PER_FLOAT);
	protected static final int TANGENT_BYTE_OFFSET = NORMAL_BYTE_OFFSET + (NORMAL_COMPONENTS * Constants.BYTES_PER_FLOAT);
	protected static final int COLOR_BYTE_OFFSET = TANGENT_BYTE_OFFSET + (TANGENT_COMPONENTS * Constants.BYTES_PER_FLOAT);

	protected static final int STRIDE = TOTAL_COMPONENTS * Constants.BYTES_PER_FLOAT;

	protected VertexBuffer vertexBuffer;
	protected IndexBuffer indexBuffer;

	protected int numVertices;
	protected int numIndices;

	protected float[] vertices;
	protected int[] indices;


	// Collision primitives
	protected List<CollisionSphere> collisionSpheres = new ArrayList<CollisionSphere>();
	protected List<CollisionCylinder> collisionCylinders = new ArrayList<CollisionCylinder>();


	public StaticMesh()
	{

	}

	public StaticMesh(Context context, int resourceId)
	{
		loadFromFile(context, resourceId);
		initialize();
	}


	private void loadFromFile(Context context, int resourceId)
	{
		try
		{
			InputStream inputStream = context.getResources().openRawResource(resourceId);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String nextLine;

			int verticesOffset = 0;
			int elementsOffset = 0;

			while((nextLine = bufferedReader.readLine()) != null)
			{
				// Split the line into tokens separated by spaces
				String[] tokens = nextLine.split(" ");

				// Check the first token of the line
				if(tokens[0].equals("VERTICES"))
				{
					// Get the number of vertices and initialize its array
					numVertices = Integer.parseInt(tokens[1]);
					vertices = new float[TOTAL_COMPONENTS * numVertices];
				}
				if(tokens[0].equals("FACES"))
				{
					// Get the number of faces and initialize the indices array
					numIndices = Integer.parseInt(tokens[1]);
					indices = new int[3 * numIndices];
				}
				else if(tokens[0].equals("VERTEX"))
				{
					// Read a vertex: <position(3)> <uvs(2)> <normal(3)> <tangent(4)>

					// Vertex position (3 components: X Y Z)
					vertices[verticesOffset++] = Float.parseFloat(tokens[1]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[2]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[3]);

					// Vertex texture coordinate (2 components: X Y)
					vertices[verticesOffset++] = Float.parseFloat(tokens[4]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[5]);

					// Vertex normal (3 components: X Y Z)
					vertices[verticesOffset++] = Float.parseFloat(tokens[6]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[7]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[8]);

					// Vertex tangent (4 components: X Y Z W)
					vertices[verticesOffset++] = Float.parseFloat(tokens[9]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[10]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[11]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[12]);

					// Vertex colors (3 components: R G B)
					vertices[verticesOffset++] = Float.parseFloat(tokens[13]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[14]);
					vertices[verticesOffset++] = Float.parseFloat(tokens[15]);
				}
				else if(tokens[0].equals("FACE"))
				{
					// Read the face indices (triangle)
					indices[elementsOffset++] = Integer.parseInt(tokens[1]);
					indices[elementsOffset++] = Integer.parseInt(tokens[2]);
					indices[elementsOffset++] = Integer.parseInt(tokens[3]);
				}
				else if(tokens[0].equals("COLLISION_SPHERE"))
				{
					// Read a collision sphere (center(X Y Z) & radius)
					CollisionSphere collisionSphere = new CollisionSphere(
							new vec3(
									Float.parseFloat(tokens[1]),
									Float.parseFloat(tokens[2]),
									Float.parseFloat(tokens[3])),
							Float.parseFloat(tokens[4]));

					collisionSpheres.add(collisionSphere);
				}
				else if(tokens[0].equals("COLLISION_CYLINDER"))
				{
					// Read a collision cylinder ( bottom(X Y Z) top(X Y Z) & radius)
					CollisionCylinder collisionCylinder = new CollisionCylinder(
							new vec3(
									Float.parseFloat(tokens[1]),
									Float.parseFloat(tokens[2]),
									Float.parseFloat(tokens[3])),
							new vec3(
									Float.parseFloat(tokens[4]),
									Float.parseFloat(tokens[5]),
									Float.parseFloat(tokens[6])),
							Float.parseFloat(tokens[7]));

					collisionCylinders.add(collisionCylinder);
				}
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not open resource: " + resourceId, e);
		}
		catch (Resources.NotFoundException nfe)
		{
			throw new RuntimeException("Resource not found: " + resourceId, nfe);
		}
	}


	public void initialize()
	{
		vertexBuffer = new VertexBuffer(vertices);
		indexBuffer = new IndexBuffer(indices);
	}


	public void reinitialize()
	{
		vertexBuffer.update(vertices);
	}


	public void bind()
	{

	}


	public void draw()
	{

	}


	public void delete()
	{

	}

	public int getVertexBufferId()
	{
		return vertexBuffer.getBufferId();
	}
}
