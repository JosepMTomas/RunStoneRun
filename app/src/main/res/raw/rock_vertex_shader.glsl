uniform mat4 u_ModelMatrix;
uniform mat4 u_ModelViewProjectionMatrix;

attribute vec4 a_Position;
attribute vec2 a_TexCoord;
attribute vec3 a_Normal;
attribute vec4 a_Tangent;

varying vec4 v_Position;
varying vec2 v_TexCoord;
varying vec3 v_Normal;
varying vec4 v_Tangent;

void main()
{
	v_Position = u_ModelViewProjectionMatrix * a_Position;
	v_TexCoord = a_TexCoord;
	v_Normal = (u_ModelMatrix * vec4(a_Normal, 1.0)).xyz;
	v_Normal = normalize(v_Normal);
	v_Tangent = u_ModelMatrix * a_Tangent;
	
	gl_Position = v_Position;
}