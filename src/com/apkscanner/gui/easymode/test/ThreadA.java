package com.apkscanner.gui.easymode.test;

public class ThreadA {
	
	static Boolean lock = Boolean.FALSE;
	
    @SuppressWarnings("static-access")
	public static void main(String[] args){
        ThreadB b = new ThreadB(lock);
        b.start();
 
        synchronized(lock){
            try{
            	
            	lock.valueOf(true);
            	
                System.out.println("Waiting for b to complete..." + lock);
                lock.wait();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
 
            System.out.println("Total is: " + b.total);
        }
    }
}
 
class ThreadB extends Thread{
    int total;
    Boolean lock;
    ThreadB(Boolean lock) {
    	this.lock = lock;
    }
    @Override
    public void run(){
        synchronized(this.lock){
            for(int i=0; i<10000 ; i++){
                total += i;
            }
            try {
				sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            this.lock.notify();
        }
    }
}