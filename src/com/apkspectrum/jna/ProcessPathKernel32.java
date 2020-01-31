package com.apkspectrum.jna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

public interface ProcessPathKernel32 extends Kernel32 {
	class MODULEENTRY32 extends Structure {
		public static class ByReference extends MODULEENTRY32 implements Structure.ByReference {
			public ByReference() {
			}

			public ByReference(Pointer memory) {
				super(memory);
			}
		}
		public MODULEENTRY32() {
			dwSize = new WinDef.DWORD(size());
		}

		public MODULEENTRY32(Pointer memory) {
			super(memory);
			read();
		}

		public DWORD dwSize;
		public DWORD th32ModuleID;
		public DWORD th32ProcessID;
		public DWORD GlblcntUsage;
		public DWORD ProccntUsage;
		public Pointer modBaseAddr;
		public DWORD modBaseSize;
		public HMODULE hModule;
		public char[] szModule = new char[255+1]; // MAX_MODULE_NAME32
		public char[] szExePath = new char[MAX_PATH];
		public String szModule() { return Native.toString(this.szModule); }
		public String szExePath() { return Native.toString(this.szExePath); }
		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList(new String[] {
					"dwSize", "th32ModuleID", "th32ProcessID", "GlblcntUsage", "ProccntUsage", "modBaseAddr", "modBaseSize", "hModule", "szModule", "szExePath"
			});
		}
	}

	ProcessPathKernel32 INSTANCE = (ProcessPathKernel32)Native.loadLibrary(ProcessPathKernel32.class, W32APIOptions.UNICODE_OPTIONS);
	boolean Module32First(HANDLE hSnapshot, MODULEENTRY32.ByReference lpme);
	boolean Module32Next(HANDLE hSnapshot, MODULEENTRY32.ByReference lpme);
}