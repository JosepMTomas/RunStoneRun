#version 300 es

layout (location = 0) in vec4 aPosition;

uniform mat4 model;
uniform mat4 viewProjection;
uniform int lod;
uniform float dist;

out vec4 vColor;

mat4 modelViewProjection = viewProjection * model;

void main()
{
	switch(lod)
	{
		case 0:
			vColor = vec4(0.0, 0.0, 1.0, 1.0); break;
		case 1:
			vColor = vec4(1.0, 0.0, 1.0, 1.0); break;
		default:
			vColor = vec4(0.0, 0.0, 0.0, 0.0); break;
	}
	
	//vColor *= dist;

	gl_Position = modelViewProjection * aPosition;
}