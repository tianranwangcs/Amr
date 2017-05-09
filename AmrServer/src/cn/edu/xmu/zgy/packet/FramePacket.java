package cn.edu.xmu.zgy.packet;

//blog.csdn.net/zgyulongfei
//Email: zgyulongfei@gmail.com

public class FramePacket {
	byte[] frame;

	public FramePacket(FramePacket fp) {
		// 传进来的fp的frame存在data中
		byte[] data = fp.getFrame();
		// copy data 中的数据到这个FramePacket的frame中
		frame = new byte[data.length];
		System.arraycopy(data, 0, frame, 0, data.length);
	}

	public FramePacket(byte[] f, int len) {
		// 和上面一样的copy 只是限定了copy的长度大小
		frame = new byte[len];
		System.arraycopy(f, 0, frame, 0, len);
	}

	public byte[] getFrame() {
		return frame;
	}
}