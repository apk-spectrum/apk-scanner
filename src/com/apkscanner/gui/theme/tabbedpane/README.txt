
•PSTabbedPaneUI - Apache License, Version 2.0
•PPTTabbedPaneUI - Apache License, Version 2.0 
•CWTabbedPaneUI - Apache License, Version 2.0  

•AquaBarTabbedPaneUI - unknown license
•PlasticTabbedPaneUI - unknown license

------------------------------------------
I got it from the site below.

http://www.jroller.com/DhilshukReddy/entry/custom_jtabbedpane


 Thursday November 22, 2007 

 Custom JTabbedPane 
JTabbedPane can be cutomized using the BasicTabbedPaneUI. 
all that you need it do is write your own class UIdelegate class extending the BasicTabbedPaneUI class 
 public class MyTabbedPaneUI extends BasicTabbedPaneUI
{ } 
 Thanks to Jon Lipsky’s for his excellent article on Creating a custom UI delegate for JTabbedPane. He has clearly explained 3 tabbed Pane UI class very neatly. 
•PSTabbedPaneUI 
•PPTTabbedPaneUI 
•CWTabbedPaneUI  

The Screenshots: 
PSTabbedPaneUI 
 
Using PSTabbedPaneUI 

JTabbedPane tabbedPane = new JTabbedPane(); 
tabbedPane.setUI(new PSTabbedPaneUI()); 
PSTabbedPaneUI Screenshot 

 

Source Code ,  
PSTabbedPaneUI.java 
 You can also download from the original link PSTabbedPaneUI.java 

Using PPTTabbedPaneUI

JTabbedPane tabbedPane = new JTabbedPane(); 
tabbedPane.setUI(new PPTTabbedPaneUI()); 
PPTTabbedPaneUI Screenshot 

 

Source Code ,  
PPTTabbedPaneUI.java 
 You can also download from the original link PPTTabbedPaneUI.java 

Using CWTabbedPaneUI 

JTabbedPane tabbedPane = new JTabbedPane(); 
tabbedPane.setUI(new CWTabbedPaneUI ()); 
CWTabbedPaneUI Screenshot 

 

Source Code ,  
CWTabbedPaneUI.java 
 You can also download from the original link CWTabbedPaneUI.java 

Other JTabbedPaneUIs 
•JGoodies Plastic TabbedPaneUI
•AquaBarTabbedPaneUI
•RomainGuy's ModernTabbedPaneUI


JGoodies Plastic TabbedPaneUI

JTabbedPane tabbedPane = new JTabbedPane(); 
tabbedPane.setUI(new PlasticTabbedPaneUI()); 
PlasticTabbedPaneUI Screenshot 

 

Source Code ,  
PlasticTabbedPaneUI.java 

AquaBarTabbedPaneUI

JTabbedPane tabbedPane = new JTabbedPane(); 
tabbedPane.setUI(new AquaBarTabbedPaneUI()); 
AquaBarTabbedPaneUI Screenshot 

 

Source Code ,  
AquaBarTabbedPaneUI.java 

ModernTabbedPaneUI

JTabbedPane tabbedPane = new JTabbedPane(); 
tabbedPane.setUI(new ModernTabbedPaneUI()); 
ModernTabbedPaneUI Screenshot 

 

Source Code ,  
ModernTabbedPaneUI.java 
 for this you need to download set of tab images at: 
tab-aqua-highlight-sep.png 
tab-aqua-highlight.png 
tab-aqua-sep.png 
tab-aqua.PNG 
tab-normal-highlight-sep.png 
tab-normal-highlight.png 
tab-normal-sep.png 
tab-normal.png 

You also need message.properies 
Open NodePad and paste the below Lines and save as message.properties 


tabUI.tabSelectedPressedEnd=/tabImages/tab-aqua-highlight-sep.png 
tabUI.tabSelectedPressed=/tabImages/tab-aqua-highlight.png 
tabUI.tabSelectedEnd=/tabImages/tab-aqua-sep.png 
tabUI.tabSelected=/tabImages/tab-aqua.png 
tabUI.tabClosePressed=/tabImages/tab-close-pressed.png 
tabUI.tabCloseRollover=/tabImages/tab-close-rollover.png 
tabUI.tabClose=/tabImages/tab-close.png 
tabUI.tabRolloverEnd=/tabImages/tab-normal-highlight-sep.png 
tabUI.tabRollover=/tabImages/tab-normal-highlight.png 
tabUI.tabEnd=/tabImages/tab-normal-sep.png 
tabUI.tab=/tabImages/tab-normal.png 

Screenshot of all JTabbedPaneUIs 

 

 Posted by Jeeru ( Nov 22 2007, 08:18:45 AM EST ) Permalink Comments [8] 
 

Comments:


Can you wrap all files into a zp file and send them to me? 

Posted by Richard on November 22, 2007 at 11:36 PM EST # 

Hi, 
 Richard I will make a jar file of all these files and post in this weblog soon. 

Posted by Dhilshuk Reddy on November 23, 2007 at 12:01 AM EST # 

JTabbedPane can be cutomized using the BasicTabbedPaneUI. 
all that you need it do is write your own class UIdelegate class extending the BasicTabbedPaneUI class 
public class MyTabbedPaneUI extends BasicTabbedPaneUI 
{ } 
Thanks to Jon Lipsky’s for his excellent article on Creating a custom UI delegate for JTabbedPane. He has clearly explained 3 tabbed Pane UI class very neatly. 

Posted by java on December 28, 2007 at 04:37 AM EST # 

JTabbedPane can be cutomized using the BasicTabbedPaneUI. 
all that you need it do is write your own class UIdelegate class extending the BasicTabbedPaneUI class 
public class MyTabbedPaneUI extends BasicTabbedPaneUI 
{ } 
Posted by java on December 28, 2007 at 04:38 AM EST # 

Sadly, most of the tabbed pane UI delegates work correctly only when the tab placement is set to TOP. The code needs to be tweaked in order for it to work with all tab placements (LEFT, RIGHT, BOTTOM) 
Posted by Francis on February 06, 2008 at 12:56 PM EST # 

Nice work! Thanks for Ideas 
Posted by Johannes Weil on July 21, 2008 at 12:00 PM EDT # 

Thanks for the sample code. Can you please explain how to customize the navigation buttons (next and previous) when TabLayoutPolicy is SCROLL_TAB_LAYOUT 
Posted by Henry Jacob on October 26, 2010 at 08:26 AM EDT # 

Respected Sir, 
I am using netbeans 7.2 and using drag and drop policy for designing which makes my class to extend 
javax.swing.JFrame. In this case I cant extend an another class..(BasicTabbedPaneUI) 

Would you plesse help me to solve this problem... 
Posted by mayur on December 27, 2012 at 06:59 AM EST # 
