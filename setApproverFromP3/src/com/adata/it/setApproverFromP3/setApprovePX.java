package com.adata.it.setApproverFromP3;

import java.util.ArrayList;
import java.util.StringTokenizer;

import com.agile.api.APIException;
import com.agile.api.IAgileClass;
import com.agile.api.IAgileSession;
import com.agile.api.IAttribute;
import com.agile.api.IChange;
import com.agile.api.IDataObject;
import com.agile.api.IItem;
import com.agile.api.INode;
import com.agile.api.IProperty;
import com.agile.api.ITableDesc;
import com.agile.api.IUser;
import com.agile.api.ItemConstants;
import com.agile.api.PropertyConstants;
import com.agile.api.TableTypeConstants;
import com.agile.px.ActionResult;
import com.agile.px.ICustomAction;

public class setApprovePX implements ICustomAction {

	public ActionResult doAction(IAgileSession session , INode actionNode , IDataObject affectedObject){
		try {
			IChange change = (IChange) affectedObject;


System.out.println(change);
			
			
			IAgileClass cls = change.getAgileClass();
			
			ArrayList result = new ArrayList();
			
			// Check if the class is abstract or concrete
			if (!cls.isAbstract()) {
				IAttribute[] attrs = null;

				//obj.logMonitor(cls.getName());	
				
				ArrayList identifyUsersList = new ArrayList();   
				
				//Get the attributes for Page Three
				ITableDesc page3 = cls.getTableDescriptor(TableTypeConstants.TYPE_PAGE_THREE);
				if (page3 != null) {
					attrs = page3.getAttributes();
					
					// 抓出下一關的關卡名稱 (name 欄位)
					//String next_status = change.getDefaultNextStatus().toString();
					
					System.out.println(change.getStatus());
					
					String v_next_status = "::" + change.getStatus().toString() + "::" ;
					
					for (int i = 0; i < attrs.length; i++) {
					
						IAttribute attr = attrs[i];				
						
						
						if (attr.isVisible()) {


							// 抓出 Description 欄位
							// 若Description 欄，為[SetApprover]開頭，表示此欄用來指定關卡簽核人員
							// [SetApprover]::REVIEW::	<--- 指定為REVIEW關卡的簽核人員
							// [SetApprover]::ME::PUR::	<--- 指定為ME和PUR關卡的簽核人員
							// 一個欄位可用來指定多關關卡
							
							IProperty v_desc = attr.getProperty(PropertyConstants.PROP_DESCRIPTION);							
							String v_desc_string = v_desc.toString();
							
							
							// 會簽關卡欄位，只會是LIST 、 multilist 二種型態之一					
							
							if ((attr.getDataType() == 4 || attr.getDataType() == 5 ) && v_desc_string.indexOf("[SetApprover]") != -1 && v_desc_string.indexOf(v_next_status) != -1) {
								//抓api name
														
								//抓出欄位值，當簽核人員
								attr.getId();
								String identifyUsers = change.getValue(attr.getId()).toString();
								
								System.out.println(attr.getId());

								
								 if(identifyUsers != null && identifyUsers.length() > 0)
								   {
									  StringTokenizer st = new StringTokenizer(identifyUsers,";");
									  while (st.hasMoreTokens()) {
										 //identifyUsersList.add(st.nextToken());
										  
										  
										  String v_userid = st.nextToken();
										  
										  v_userid=v_userid.substring(v_userid.indexOf("(")+1);
										  v_userid = v_userid.substring(0,v_userid.length()-1);
										  System.out.println(v_userid);
										  


										  IUser user = (IUser)session.getObject(IUser.OBJECT_TYPE,v_userid);
										  IUser[] approvers = new IUser[]{user};
										  
										  try{
											  change.addApprovers(change.getStatus(), approvers, null, true,"");  
										  }catch(APIException e) {
											  
										  }
										  
										  //System.out.println(change.getStatus());
											
									  }
								   }   
								
							}
							
						
						}
						
					}
					
				}
			}
			
			
			IUser user_admin = (IUser)session.getObject(IUser.OBJECT_TYPE,"admin");
			IUser[] approvers_admin = new IUser[]{user_admin};
			try{
				  
				  //change.removeApprovers(change.getDefaultNextStatus(), approvers_admin, null, "");
				  change.removeApprovers(change.getStatus(), approvers_admin, null, "");
				  
				  
			  }catch(APIException e) {
				  //e.printStackTrace();
			  }
			
			
			return new ActionResult (ActionResult.STRING , "success");
			
		} catch (Exception e){
			e.printStackTrace();
			return new ActionResult (ActionResult.EXCEPTION, e);
		}
	}
}
