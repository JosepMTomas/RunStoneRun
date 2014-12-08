#version 300 es
precision highp float;

uniform sampler2D reflectionTexture;

in vec4 v_Position;
in vec2 v_TexCoord;
in vec3 v_Normal;

out vec4 fragColor;

void main()
{
	vec2 texCoord = vec2(1.0 - v_TexCoord.x, v_TexCoord.y);
	//vec2 texCoord = v_TexCoord;
	vec4 reflection = texture(reflectionTexture, texCoord);

	vec3 color = vec3(1.0, 0.0, 0.0);
	vec3 vLight = vec3(0.0, 1.0, 0.0);
	
	float diffuse = dot(v_Normal, vLight);
	diffuse = max(diffuse, 0.0);
	
	vec3 result = color * diffuse;
	
	fragColor = reflection * 0.75;
}