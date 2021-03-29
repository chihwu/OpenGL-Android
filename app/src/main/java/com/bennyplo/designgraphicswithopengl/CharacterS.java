package com.bennyplo.designgraphicswithopengl;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class CharacterS {
    private final String vertexShaderCode =
            "attribute vec3 aVertexPosition;"+//vertex of an object
                    " attribute vec4 aVertexColor;"+//the colour  of the object
                    "     uniform mat4 uMVPMatrix;"+//model view  projection matrix
                    "    varying vec4 vColor;"+//variable to be accessed by the fragment shader
                    "    void main() {" +
                    "        gl_Position = uMVPMatrix* vec4(aVertexPosition, 1.0);"+//calculate the position of the vertex
                    "        vColor=aVertexColor;}";//get the colour from the application program

    private final String fragmentShaderCode =
            "precision mediump float;"+ //define the precision of float
                    "varying vec4 vColor;"+ //variable from the vertex shader
                    "void main() {"+
                    "   gl_FragColor = vColor; }";//change the colour based on the variable from the vertex shader

    private final FloatBuffer vertexBuffer,colorBuffer;
    private final IntBuffer indexBuffer;
    private final int mProgram;
    private int mPositionHandle,mColorHandle;
    private int mMVPMatrixHandle;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int COLOR_PER_VERTEX = 4;
    private int vertexCount;// number of vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int colorStride=COLOR_PER_VERTEX*4;//4 bytes per vertex
    float CharSVertex[] ={
    };
    static int CharSIndex[]={
    };
    static float CharSColor[]={
    };
    static float P[] = {2, 0, 3.2f, 0, 4, 0.8f, 2.8f, 1.3f, 2, 1.5f, 2, 2, 3.2f, 2};  // control points
    static float Q[] = {2, 0.2f, 2.2f, 0.2f, 3.6f, 0.4f, 2.8f, 1, 1.5f, 1.5f, 1.6f, 2.2f, 3.2f, 2.2f};

    private void createCurve(float control_pts_p[], float control_pts_Q[]) {
        float vertices[] = new float[65535];
        float color[] = new float[65535];
        int index[] = new int[65535];
        int vi = 0, cindx = 0, indx = 0, px = 0;
        double x, y;
        float z = 0.2f, centroidx = 0, centroidy = 0;
        int nosegments = (control_pts_p.length / 2) / 3;
        for (int i = 0; i < control_pts_p.length; i+=2) {
            centroidx += control_pts_p[i];
            centroidy += control_pts_p[i + 1];
        }
        centroidx /= (float)(control_pts_p.length / 2);
        centroidy /= (float)(control_pts_p.length / 2);
        for (int segments = 0; segments < nosegments; segments++) {
            for (float t = 0; t < 1.0f; t += 0.1f) {
                x = Math.pow(1 - t, 3) * control_pts_p[px] + control_pts_p[px + 2] * 3 * t * Math.pow(1 - t, 2) + control_pts_p[px + 4] * 3 * t * t * (1 - t) + control_pts_p[px + 6] * Math.pow(t, 3);
                y = Math.pow(1 - t, 3) * control_pts_p[px + 1] + control_pts_p[px + 3] * 3 * t * Math.pow(1 - t, 2) + control_pts_p[px + 5] * 3 * t * t * (1 - t) + control_pts_p[px + 7] * Math.pow(t, 3);
                vertices[vi++] = (float)x - centroidx;
                vertices[vi++] = (float)y - centroidy;
                vertices[vi++] = z;
                color[cindx++] = 1;
                color[cindx++] = 1;
                color[cindx++] = 0;
                color[cindx++] = 1;
            }
            px += 6;
        }
        px = 0;
        int vj = vi;
        for (int segments = 0; segments < nosegments; segments++) {
            for (float t = 0; t < 1.0f; t += 0.1f) {
                x = Math.pow(1 - t, 3) * control_pts_Q[px] + control_pts_Q[px + 2] * 3 * t * Math.pow(1 - t, 2) + control_pts_Q[px + 4] * 3 * t * t * (1 - t) + control_pts_Q[px + 6] * Math.pow(t, 3);
                y = Math.pow(1 - t, 3) * control_pts_Q[px + 1] + control_pts_Q[px + 3] * 3 * t * Math.pow(1 - t, 2) + control_pts_Q[px + 5] * 3 * t * t * (1 - t) + control_pts_Q[px + 7] * Math.pow(t, 3);
                vertices[vj++] = (float)x - centroidx;
                vertices[vj++] = (float)y - centroidy;
                vertices[vj++] = z;
                color[cindx++] = 1;
                color[cindx++] = 1;
                color[cindx++] = 0;
                color[cindx++] = 1;
            }
            px += 6;
        }
        //

        int novertices = vj;   // vj here represents the total number of vertices for a 2-d S with two curves
        // set up indexes for the front
        for (int v0=0,v1=1,v2=vi/3,v3=vi/3+1; v3<novertices/3; v0++, v1++, v2++, v3++) {
            index[indx++] = v0;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v3;
        }
        //

        // create new vertices for the back
        int vk = novertices;
        for (int i = 0; i < novertices;) {
            vertices[vk++] = vertices[i++];
            vertices[vk++] = vertices[i++];
            vertices[vk++] = -vertices[i++];

            color[cindx++] = 1;
            color[cindx++] = 0;
            color[cindx++] = 0;
            color[cindx++] = 1;
        }
        //

        novertices = vk;  // update the total of vertices
        // back
        for (int v0=vj/3,v1=vj/3+1,v2=(vi+vj)/3,v3=(vi+vj)/3+1; v3<novertices/3; v0++, v1++, v2++, v3++) {
            index[indx++] = v0;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v3;
        }
        // bottom
        for (int v0=0,v1=1,v2=vj/3,v3=vj/3+1; v3<(vi + vj)/3; v0++, v1++, v2++, v3++) {
            index[indx++] = v0;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v3;
        }
        // top
        for (int v0=vi/3,v1=vi/3+1,v2=(vi+vj)/3,v3=(vi+vj)/3+1; v3<novertices/3; v0++, v1++, v2++, v3++) {
            index[indx++] = v0;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v1;
            index[indx++] = v2;
            index[indx++] = v3;
        }
        CharSVertex = Arrays.copyOf(vertices, novertices);
        CharSIndex = Arrays.copyOf(index, indx);
        CharSColor = Arrays.copyOf(color, cindx);
    }

    public CharacterS() {
        createCurve(P, Q);
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(CharSVertex.length * 4);// (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(CharSVertex);
        vertexBuffer.position(0);
        vertexCount=CharSVertex.length/COORDS_PER_VERTEX;
        ByteBuffer cb=ByteBuffer.allocateDirect(CharSColor.length * 4);// (# of coordinate values * 4 bytes per float)
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(CharSColor);
        colorBuffer.position(0);
        IntBuffer ib=IntBuffer.allocate(CharSIndex.length);
        indexBuffer=ib;
        indexBuffer.put(CharSIndex);
        indexBuffer.position(0);
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
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES32.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyRenderer.checkGlError("glGetUniformLocation");
    }

    public void draw(float[] mvpMatrix) {
        GLES32.glUseProgram(mProgram);//use the object's shading programs
        // Apply the projection and view transformation
        GLES32.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyRenderer.checkGlError("glUniformMatrix4fv");
        //set the attribute of the vertex to point to the vertex buffer
        GLES32.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES32.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES32.GL_FLOAT, false, colorStride, colorBuffer);
        // Draw the 3D character A
        GLES32.glDrawElements(GLES32.GL_TRIANGLES,CharSIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
//        GLES32.glDrawElements(GLES32.GL_LINES,CharSIndex.length,GLES32.GL_UNSIGNED_INT,indexBuffer);
    }
}
