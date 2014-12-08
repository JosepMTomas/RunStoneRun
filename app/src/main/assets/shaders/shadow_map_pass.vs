#version 300 es
  
layout(location=0) in vec4 aPosition;

uniform mat4 modelViewProjection;

void main()
{ 	 
	//get the clipspace vertex position by multiplying the object space vertex 
	//position with the combined modelview project matrix
	gl_Position = modelViewProjection * aPosition;
}