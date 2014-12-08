#version 300 es

layout (location = 0) in vec4 aPosition;

uniform mat4 uModelViewProjection;
uniform mat4 uModelView;

out vec4 vPosition;
out vec3 vEye;
out float vDepth;

void main()
{
	vPosition = aPosition;
	
	vec4 viewPosition = uModelView * aPosition;
	vEye = normalize(viewPosition.xyz);
	vDepth = distance(viewPosition, vec4(0.0)) / 500.0;
	
	gl_Position = uModelViewProjection * aPosition;
}