precision highp float;

uniform sampler2D u_NormalTexture;

uniform vec3 u_EyePosition;

varying vec4 v_Position;
varying vec2 v_TexCoord;
varying vec3 v_Normal;
varying vec4 v_Tangent;

void main()
{
	vec3 binormal = normalize(cross(v_Normal, v_Tangent.xyz));// * v_Tangent.w;
	vec4 normalTex = texture2D(u_NormalTexture, v_TexCoord * vec2(3.0));
	normalTex = normalize(normalTex * 2.0 - 1.0);
	
	//normal = intersection.tangent*normal.x + binormal*normal.y + intersection.normal*normal.z;
	vec3 normal = v_Tangent.xyz * normalTex.x + binormal * -normalTex.y + v_Normal * normalTex.z;
	
	float diffuse = max(dot(normal, vec3(0,1,0)), 0.0);
	float ambient = max(dot(normal, vec3(0,-1,0)), 0.0);
	ambient = ambient * 0.5;
	
	vec3 color = vec3(diffuse);
	//color = color + (vec3(ambient) * vec3(1.0,0.7,0.2));
	color = color + (vec3(ambient) * vec3(0.2,1.0,0.2));
	
	//vec3 vEye = normalize(v_Position.xyz - u_EyePosition);
	//diffuse = dot(normal, vEye);
	
	//gl_FragColor = vec4(normal, 0.0);
	gl_FragColor = vec4(color, 0.0);
	//gl_FragColor = vec4(normal, 0.0);
	//gl_FragColor = v_Tangent;
}