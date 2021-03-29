package com.bennyplo.designgraphicswithopengl;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class ArbitraryShapeWithDirectionalLight {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+//vertex of an object
                    " attribute vec4 aVertexColor;"+//the colour  of the object
                    "     uniform mat4 uMVPMatrix;"+//model view  projection matrix
                    "    varying vec4 vColor;"+//variable to be accessed by the fragment shader
                    "    uniform vec3 uPointLightingLocation;" +
                    "    varying float vPointLightWeighting;" +
                    // ----------------------------------------------
                    "    attribute vec3 aVertexNormal;" +
                    "    uniform vec3 uDiffuseLightLocation;" +
                    "    uniform vec4 uDiffuseColor;" +
                    "    varying vec4 vDiffuseColor;" +
                    "    varying float vDiffuseLightWeighting;" +
                    "    uniform vec3 uAttenuation;" +
                    // ----------------------------------------------
                    "    void main() {" +
                    "        vec4 mvPosition = uMVPMatrix * vec4(aVertexPosition, 1.0);" +
                    "        vec3 lightDirection = normalize(uPointLightingLocation - mvPosition.xyz);" +
                    "        float dist_from_light = distance(uPointLightingLocation, mvPosition.xyz);" +
                    "        vPointLightWeighting = 18.0 / (dist_from_light*dist_from_light);" +
                    "        gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);"+//calculate the position of the vertex
                    // ------------------------
                    "        vec3 diffuseLightDirection = normalize(uDiffuseLightLocation - mvPosition.xyz);" +
                    "        vec3 transformedNormal = normalize((uMVPMatrix * vec4(aVertexNormal, 0.0)).xyz);" +
                    "        vDiffuseColor = uDiffuseColor;" +
                    "        vec3 vertexToLightSource = uDiffuseLightLocation - mvPosition.xyz;" +
                    "        float diff_light_dist = length(vertexToLightSource);" +
                    "        float attenuation = 1.0 / (uAttenuation.x + uAttenuation.y * diff_light_dist + uAttenuation.z * diff_light_dist * diff_light_dist);"  +
                    "        vDiffuseLightWeighting = attenuation * max(dot(transformedNormal, diffuseLightDirection), 0.0);" +
                    // ------------------------
                    "        vColor=aVertexColor;}";//get the colour from the application program

    private final String fragmentShaderCode =
            "precision mediump float;"+ //define the precision of float
                    "varying vec4 vColor;"+ //variable from the vertex shader
                    "varying float vPointLightWeighting;" +
                    // ----------------------
                    "varying vec4 vDiffuseColor;" +
                    "varying float vDiffuseLightWeighting;" +
                    // ----------------------
                    "void main() {"+
                    "     vec4 diffuseColor = vDiffuseLightWeighting * vDiffuseColor;" +
                    "     gl_FragColor = vColor + diffuseColor;}";
//                    "   gl_FragColor = vec4(vColor.xyz * vPointLightWeighting, 1.0); }";//change the colour based on the variable from the vertex shader
//                    "   gl_FragColor = vColor; }";//change the colour based on the variable from the vertex shader

    private final FloatBuffer vertexBuffer,colorBuffer;
    private final IntBuffer indexBuffer;
    private final FloatBuffer vertex2Buffer,color2Buffer;
    private final IntBuffer index2Buffer;
    private final FloatBuffer ringVertexBuffer, ringColorBuffer;
    private final IntBuffer ringIndexBuffer;
    private final int mProgram;
    private int mPositionHandle,mColorHandle;
    private int mMVPMatrixHandle;
    private int pointLightingLocationHandle;

    // --------------------
    private final FloatBuffer normalBuffer, normal2Buffer, ring_normalBuffer;
    private int mNormalHandle;
    private int diffuseLightLocationHandle, diffuseColorHandle, attenuateHandle;
    // --------------------

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private int vertexCount;// number of vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;//4 bytes per vertex
    float SphereVertex[] ={};
    static int SphereIndex[]={};
    static float SphereColor[]={};
    // 2nd sphere
    float Sphere2Vertex[] ={};
    static int Sphere2Index[]={};
    static float Sphere2Color[]={};
    // ring
    float ringVertex[] ={};
    static int ringIndex[]={};
    static float ringColor[]={};

    static float lightLocation[] = new float[3];

    // ------------------
    static float SphereNormal[];
    static float Sphere2Normal[];
    static float ringNormal[];
    static float diffuseLightLocation[] = new float[3];
    static float diffuseColor[] = new float[4];
    static float attenuation[] = new float[3];
    // ------------------

    private void createSphere(float radius, int nolatitude, int nolongitude) {
        float vertices[] = new float[65535];
        int index[] = new int[65535];
        float color[] = new float[65535];

        // -----------------
        float normals[] = new float[65535];
        int normalindx = 0;
        float normals2[] = new float[65535];
        int normal2indx = 0;
        float ringnormal[] = new float[65535];
        int ringnormalindx = 0;
        int pnormlen = (nolongitude + 1) * 3 * 3;
        // -----------------

        int vertexIndex = 0;
        int colorIndex = 0;
        int indx = 0;
        float vertices2[] = new float[65535];
        int index2[] = new int[65535];
        float color2[] = new float[65535];
        int vertex2Index = 0;
        int color2Index = 0;
        int indx2 = 0;
        // ring
        float ring_vertices[] = new float[65535];
        int ring_index[] = new int[65535];
        float ring_color[] = new float[65535];
        int rvindx = 0;
        int rcindx = 0;
        int rindx = 0;
        float dist = 3;
        int plen = (nolongitude+1)*3*3;   // horizontal plane length   Note: the last factor of 3 is for counting the 3 components in the top cone
        int pcolorlen = (nolongitude+1)*4*3;   // horizontal plane color length
        for (int row = 0; row <= nolatitude; row++) {
            double theta = row * Math.PI / nolatitude;
            double sinTheta = Math.sin(theta);
            double cosTheta = Math.cos(theta);
            float tcolor = -0.5f;
            float tcolorinc = 1/(float)(nolongitude+1);
            for (int col = 0; col <= nolongitude; col++) {
                double phi = col * 2 * Math.PI / nolongitude;
                double sinPhi = Math.sin(phi);
                double cosPhi = Math.cos(phi);
                double x = cosPhi * sinTheta;
                double y = cosTheta;
                double z = sinPhi * sinTheta;
                vertices[vertexIndex++] = (float)(radius * x);
                vertices[vertexIndex++] = (float)(radius * y) + dist;
                vertices[vertexIndex++] = (float)(radius * z);

                vertices2[vertex2Index++] = (float)(radius * x);
                vertices2[vertex2Index++] = (float)(radius * y) - dist;
                vertices2[vertex2Index++] = (float)(radius * z);

                color[colorIndex++] = 1;
                color[colorIndex++] = Math.abs(tcolor);
                color[colorIndex++] = 0;
                color[colorIndex++] = 1;

                color2[color2Index++] = 0;
                color2[color2Index++] = 1;
                color2[color2Index++] = Math.abs(tcolor);
                color2[color2Index++] = 1;

                // ---------------------
                normals[normalindx++] = (float)(radius*x);
                normals[normalindx++] = (float)(radius*y) + dist;
                normals[normalindx++] = (float)(radius*z);
                normals2[normal2indx++] = (float)(radius*x);
                normals2[normal2indx++] = (float)(radius*y) - dist;
                normals2[normal2indx++] = (float)(radius*z);
                // ---------------------

                // Note that the total number of rows is 30 as passed
                if (row == 10) {  // bottom of top cone
                    ring_vertices[rvindx++] = (float)(radius*x)/2;
                    ring_vertices[rvindx++] = (float)(radius*y)/2-0.1f*dist;
                    ring_vertices[rvindx++] = (float)(radius*z)/2;
                    ring_color[rcindx++] = 1;
                    ring_color[rcindx++] = Math.abs(tcolor);
                    ring_color[rcindx++] = 0;
                    ring_color[rcindx++] = 1;

                    // ----------------
                    ringnormal[ringnormalindx++] = (float)(radius*x)/2;
                    ringnormal[ringnormalindx++] = (float)(radius*y)/2-0.1f*dist;
                    ringnormal[ringnormalindx++] = (float)(radius*z)/2;
                    // ----------------
                }
                if (row == 15) {  // lower part of top cone
                    ring_vertices[rvindx++] = (float)(radius*x)/2;
                    ring_vertices[rvindx++] = (float)(radius*y)/2+0.2f*dist;
                    ring_vertices[rvindx++] = (float)(radius*z)/2;
                    ring_color[rcindx++] = 1;
                    ring_color[rcindx++] = Math.abs(tcolor);
                    ring_color[rcindx++] = 0;
                    ring_color[rcindx++] = 1;

                    // -------------
                    ringnormal[ringnormalindx++] = (float)(radius*x)/2;
                    ringnormal[ringnormalindx++] = (float)(radius*y)/2+0.2f*dist;
                    ringnormal[ringnormalindx++] = (float)(radius*z)/2;
                    // -------------
                }
                if (row == 20) {   // upper part of top cone
                    ring_vertices[rvindx++] = (float)(radius*x);
                    ring_vertices[rvindx++] = (float)(radius*y)+dist;
                    ring_vertices[rvindx++] = (float)(radius*z);
                    ring_color[rcindx++] = 1;
                    ring_color[rcindx++] = Math.abs(tcolor);
                    ring_color[rcindx++] = 0;
                    ring_color[rcindx++] = 1;

                    // -------------
                    ringnormal[ringnormalindx++] = (float)(radius*x);
                    ringnormal[ringnormalindx++] = (float)(radius*y)+dist;
                    ringnormal[ringnormalindx++] = (float)(radius*z);
                    // -------------
                }
                if (row == 20) {   // bottom cone
                    ring_vertices[plen++] = (float)(radius*x);
                    ring_vertices[plen++] = (float)(-radius*y)-dist;
                    ring_vertices[plen++] = (float)(radius*z);
                    ring_color[pcolorlen++] = 0;
                    ring_color[pcolorlen++] = 1;
                    ring_color[pcolorlen++] = Math.abs(tcolor);
                    ring_color[pcolorlen++] = 1;

                    // ----------------
                    ringnormal[ringnormalindx++] = (float)(radius*x);
                    ringnormal[ringnormalindx++] = (float)(-radius*y)-dist;
                    ringnormal[ringnormalindx++] = (float)(radius*z);
                    // ----------------
                }

                tcolor += tcolorinc;
            }
        }

        // index buffer
        for (int row = 0; row < nolatitude; row++) {
            for (int col = 0; col < nolongitude; col++) {
                int P0 = (row * (nolongitude + 1)) + col;
                int P1 = P0 + nolongitude + 1;
                index[indx++] = P0;
                index[indx++] = P1;
                index[indx++] = P0 + 1;
                index[indx++] = P1;
                index[indx++] = P1 + 1;
                index[indx++] = P0 + 1;

                index2[indx2++] = P0;
                index2[indx2++] = P1;
                index2[indx2++] = P0 + 1;
                index2[indx2++] = P1;
                index2[indx2++] = P1 + 1;
                index2[indx2++] = P0 + 1;
            }
        }
        rvindx = (nolongitude+1)*3*4;
        rcindx = (nolongitude+1)*4*4;

        //-----------
        ringnormalindx=(nolongitude+1)*3*4;
        //-----------

        plen = nolongitude+1;   // total number of longitudes degrees for a plane
        for (int j = 0; j < plen - 1; j++) {
            // lower part of top cone
            ring_index[rindx++]=j;
            ring_index[rindx++]=j+plen;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+plen+1;
            ring_index[rindx++]=j+plen;

            // upper part of top cone
            ring_index[rindx++]=j+plen;
            ring_index[rindx++]=j+plen*2;
            ring_index[rindx++]=j+plen+1;
            ring_index[rindx++]=j+plen+1;
            ring_index[rindx++]=j+plen*2+1;
            ring_index[rindx++]=j+plen*2;

            // bottom cone
            ring_index[rindx++]=j;
            ring_index[rindx++]=j+plen*3;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+1;
            ring_index[rindx++]=j+plen*3+1;
            ring_index[rindx++]=j+plen*3;
        }

        SphereVertex = Arrays.copyOf(vertices, vertexIndex);
        SphereIndex = Arrays.copyOf(index, indx);
        SphereColor = Arrays.copyOf(color, colorIndex);

        Sphere2Vertex = Arrays.copyOf(vertices2, vertex2Index);
        Sphere2Index = Arrays.copyOf(index2, indx2);
        Sphere2Color = Arrays.copyOf(color2, color2Index);

        ringVertex = Arrays.copyOf(ring_vertices, rvindx);
        ringColor = Arrays.copyOf(ring_color, rcindx);
        ringIndex = Arrays.copyOf(ring_index, rindx);

        // --------------
        SphereNormal = Arrays.copyOf(normals, normalindx);
        Sphere2Normal = Arrays.copyOf(normals2, normal2indx);
        ringNormal = Arrays.copyOf(ringnormal, ringnormalindx);
        // --------------
    }

    public ArbitraryShapeWithDirectionalLight() {
        createSphere(2, 30, 30);

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(SphereVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SphereVertex);
        vertexBuffer.position(0);
        vertexCount=SphereVertex.length/COORDS_PER_VERTEX;
        ByteBuffer cb=ByteBuffer.allocateDirect(SphereColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(SphereColor);
        colorBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(SphereIndex.length);
        indexBuffer=ib;
        indexBuffer.put(SphereIndex);
        indexBuffer.position(0);
        // 2nd sphere
        ByteBuffer bb2 = ByteBuffer.allocateDirect(Sphere2Vertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb2.order(ByteOrder.nativeOrder());
        vertex2Buffer = bb2.asFloatBuffer();
        vertex2Buffer.put(Sphere2Vertex);
        vertex2Buffer.position(0);
        ByteBuffer cb2=ByteBuffer.allocateDirect(Sphere2Color.length * 4);// (# of coordinate values * 4 bytes per float)
        cb2.order(ByteOrder.nativeOrder());
        color2Buffer = cb2.asFloatBuffer();
        color2Buffer.put(Sphere2Color);
        color2Buffer.position(0);
        IntBuffer ib2=IntBuffer.allocate(Sphere2Index.length);
        index2Buffer=ib2;
        index2Buffer.put(Sphere2Index);
        index2Buffer.position(0);
        // ring
        ByteBuffer rbb = ByteBuffer.allocateDirect(ringVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        rbb.order(ByteOrder.nativeOrder());
        ringVertexBuffer = rbb.asFloatBuffer();
        ringVertexBuffer.put(ringVertex);
        ringVertexBuffer.position(0);
        ByteBuffer rcb=ByteBuffer.allocateDirect(ringColor.length * 4);// (# of coordinate values * 4 bytes per float)
        rcb.order(ByteOrder.nativeOrder());
        ringColorBuffer = rcb.asFloatBuffer();
        ringColorBuffer.put(ringColor);
        ringColorBuffer.position(0);
        IntBuffer rib=IntBuffer.allocate(ringIndex.length);
        ringIndexBuffer=rib;
        ringIndexBuffer.put(ringIndex);
        ringIndexBuffer.position(0);
        // ---------------
        ByteBuffer nb = ByteBuffer.allocateDirect(SphereNormal.length * 4);// (# of coordinate values * 4 bytes per float)
        nb.order(ByteOrder.nativeOrder());
        normalBuffer = nb.asFloatBuffer();
        normalBuffer.put(SphereNormal);
        normalBuffer.position(0);
        ByteBuffer nb2=ByteBuffer.allocateDirect(Sphere2Normal.length * 4);// (# of coordinate values * 4 bytes per float)
        nb2.order(ByteOrder.nativeOrder());
        normal2Buffer = nb2.asFloatBuffer();
        normal2Buffer.put(Sphere2Normal);
        normal2Buffer.position(0);
        ByteBuffer rrb=ByteBuffer.allocateDirect(ringNormal.length * 4);
        rrb.order(ByteOrder.nativeOrder());
        ring_normalBuffer = rrb.asFloatBuffer();
        ring_normalBuffer.put(ringNormal);
        ring_normalBuffer.position(0);
        // ---------------
        // prepare shaders and OpenGL program
        int vertexShader = MyRenderer.loadShader(GLES32.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyRenderer.loadShader(GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES32.glCreateProgram();             // create empty OpenGL Program
        GLES32.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES32.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES32.glLinkProgram(mProgram);                  // link the  OpenGL program to create an executable
        GLES32.glUseProgram(mProgram);// Add program to OpenGL environment
        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES32.glGetAttribLocation(mProgram, "aVertexPosition");
        // Enable a handle to the triangle vertices
        GLES32.glEnableVertexAttribArray(mPositionHandle);
        mColorHandle = GLES32.glGetAttribLocation(mProgram, "aVertexColor");
        // Enable a handle to the  colour
        GLES32.glEnableVertexAttribArray(mColorHandle);
        // Prepare the colour coordinate data
        GLES32.glVertexAttribPointer(mColorHandle, COLOR_PER_VERTEX, GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // --------------
        mNormalHandle = GLES32.glGetAttribLocation(mProgram, "aVertexNormal");
        GLES32.glEnableVertexAttribArray(mNormalHandle);
        diffuseLightLocationHandle = GLES32.glGetUniformLocation(mProgram, "uDiffuseLightLocation");
        diffuseColorHandle = GLES32.glGetUniformLocation(mProgram, "uDiffuseColor");
        attenuateHandle = GLES32.glGetUniformLocation(mProgram, "uAttenuation");
        diffuseLightLocation[0] = 3;
        diffuseLightLocation[1] = 2;
        diffuseLightLocation[2] = 2;
        diffuseColor[0] = 1;
        diffuseColor[1] = 1;
        diffuseColor[2] = 1;
        diffuseColor[3] = 1;
        attenuation[0] = 1;
        attenuation[1] = 0.35f;
        attenuation[2] = 0.44f;
        // --------------
        lightLocation[0] = 2;
        lightLocation[1] = 2;
        lightLocation[2] = 2;
        pointLightingLocationHandle = GLES32.glGetUniformLocation(mProgram, "uPointLightingLocation");
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyRenderer.checkGlError("glGetUniformLocation");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);//use the object's shading programs
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyRenderer.checkGlError("glUniformMatrix4fv");
        GLES32.glUniform3fv(pointLightingLocationHandle, 1, lightLocation, 0);

        // ---------------
        GLES32.glUniform3fv(diffuseLightLocationHandle, 1, diffuseLightLocation, 0);
        GLES32.glUniform4fv(diffuseColorHandle, 1, diffuseColor, 0);
        GLES32.glUniform3fv(attenuateHandle, 1, attenuation, 0);

        // ---------------

//        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // -----------
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normalBuffer);
        // -----------
//        GLES32.glDrawElements(GLES32.GL_LINES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,SphereIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);

//
        // 2nd sphere
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertex2Buffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, color2Buffer);
        // --------------
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false, vertexStride, normal2Buffer);
        // --------------
//        GLES32.glDrawElements(GLES32.GL_LINES,Sphere2Index.length,GLES32.GL_UNSIGNED_INT,index2Buffer);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,Sphere2Index.length,GLES32.GL_UNSIGNED_INT,index2Buffer);

        // ring
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, ringVertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, ringColorBuffer);
        // --------------
        GLES32.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES32.GL_FLOAT, false,vertexStride, ring_normalBuffer);
        // --------------
//        GLES32.glDrawElements(GLES32.GL_LINES,ringIndex.length,GLES32.GL_UNSIGNED_INT,ringIndexBuffer);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,ringIndex.length,GLES32.GL_UNSIGNED_INT,ringIndexBuffer);

    }

    public void setLightLocation(float px, float py, float pz) {
        lightLocation[0] = px;
        lightLocation[1] = py;
        lightLocation[2] = pz;
    }
}
