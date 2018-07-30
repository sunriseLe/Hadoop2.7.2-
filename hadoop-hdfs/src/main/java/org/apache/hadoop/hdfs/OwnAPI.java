package org.apache.hadoop.hdfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.CreateFlag;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.XAttrSetFlag;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclEntry.Builder;
import org.apache.hadoop.fs.permission.AclEntryScope;
import org.apache.hadoop.fs.permission.AclEntryType;
import org.apache.hadoop.fs.permission.AclStatus;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;
import org.apache.hadoop.hdfs.protocol.BlockStoragePolicy;
import org.apache.hadoop.hdfs.protocol.CachePoolEntry;
import org.apache.hadoop.hdfs.protocol.CachePoolInfo;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.DatanodeReportType;
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus;
import org.apache.hadoop.hdfs.protocol.SnapshottableDirectoryStatus;
import org.apache.hadoop.hdfs.security.token.block.DataEncryptionKey;
import org.apache.hadoop.hdfs.server.protocol.DatanodeStorageReport;
import org.apache.hadoop.util.Progressable;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

public class OwnAPI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			System.setProperty("HADOOP_USER_NAME", "cephlee");
			DFSClient client = new DFSClient(new URI("hdfs://192.168.202.34"),
					new Configuration());

			client.close();
			  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*xattr操作测试*/
	public static void XAttrTest(DFSClient client,String src,
			String name,String str) {
		try {
			/*设置xattr*/
			byte[] value = str.getBytes("UTF-8");
			client.setXAttr(src, name, value, 
					EnumSet.of(XAttrSetFlag.CREATE));
			client.setXAttr(src, "user.owner", value, 
					EnumSet.of(XAttrSetFlag.REPLACE));
			client.setXAttr(src, "user.key", value, 
					EnumSet.of(XAttrSetFlag.REPLACE));
			
			/*获取xattr*/
			value=client.getXAttr(src, name);
			String newStr=new String(value,"UTF-8" );
			System.out.println("获取指定name的xattr\n<key,value>:"+
					"<"+name+", "+newStr+">");
			
			
			/*获取xattrs并遍历<key,value>*/
			Map<String, byte[]> xattrs=client.getXAttrs(src);
			System.out.println("获取文件的所有xattr：");
			for(Entry<String, byte[]> entry:xattrs.entrySet()){
				System.out.println("<key,value>:"+"<"+entry.getKey()+", "+
						(new String(entry.getValue()))+">");
			}
			
			/*获取指定name的xattrs并遍历<key,value>*/
			List<String> list=new ArrayList<>();
			list.add("user.name");
			list.add("user.owner");
			xattrs=client.getXAttrs(src, list);
			System.out.println("获取文件指定name的xattr：");
			for(Entry<String, byte[]> entry:xattrs.entrySet()){
				System.out.println("<key,value>:"+"<"+entry.getKey()+", "+
						(new String(entry.getValue()))+">");
			}
			
			/*获取xattr的name列表*/
			System.out.println("获取文件xattr的name列表：");
			List<String> keys=client.listXAttrs(src);
			 for (int i = 0; i < keys.size(); i++) {
	                System.out.println(keys.get(i));  //.get(index)
	          }
			 
			 /*删除指定name的xattr*/
			 client.removeXAttr(src, name);
			 System.out.println("删除后的xattr列表");
			 Map<String, byte[]> xattrs1=client.getXAttrs(src);
			for(Entry<String, byte[]> entry:xattrs1.entrySet()){
				System.out.println("<key,value>:"+"<"+entry.getKey()+", "+
						(new String(entry.getValue()))+">");
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*AclEntries的基本操作测试*/
	public static void AclEntriesTest(DFSClient client,String src) {
		try {
			/*获取文件的aclStatus*/
			AclStatus status = client.getAclStatus(src);
			System.out.println(status.toString());
			
			/*创建新的AclEntry*/
			AclEntry.Builder builder = new Builder();
		    builder.setName("hadoop0");
		    builder.setType(AclEntryType.USER);
		    builder.setScope(AclEntryScope.DEFAULT);
		    builder.setPermission(FsAction.ALL);
		    AclEntry newEntry = builder.build();

		    List<AclEntry> aclEntries = status.getEntries();
		    aclEntries.add(newEntry);
		    
		    /*设置新的AclEntry*/
		    client.setAcl(src, aclEntries);
		    AclStatus status0=client.getAclStatus(src);
			System.out.println(status0.toString());
			
			/*修改AclEntry*/
			client.modifyAclEntries(src, aclEntries);
		    AclStatus status1=client.getAclStatus(src);
			System.out.println(status1.toString());
			
			/*删除AclEntry，3种方式*/
			client.removeAclEntries(src, aclEntries);
			AclStatus status2=client.getAclStatus(src);
			System.out.println(status2.toString());
			
			/*client.removeDefaultAcl(src);
			AclStatus status3=client.getAclStatus(src);
			System.out.println(status3.toString());
			
			client.removeAcl(src);
			AclStatus status4=client.getAclStatus(src);
			System.out.println(status4.toString());*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*ContentSummary测试*/
	public static void ContentSummaryTest(DFSClient client,String path) {
		try {
			ContentSummary summary = client.getContentSummary(path);
			System.out.println("Path:"+path+", directoryCount-->"+summary.getDirectoryCount()+
					", fileCount-->"+summary.getFileCount()+", length-->"+
					summary.getLength());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*cachePool基本操作测试*/
	public static void CachePoolTest(DFSClient client,String pool,String newOwner) {
		/*创建CachePool的配置信息*/
		CachePoolInfo poolInfo=new CachePoolInfo(pool).setOwnerName("cephlee")
				.setGroupName("supergroup").setMode(new FsPermission((short)0777))
				.setLimit(4096l);
		try {
			/*创建CachePool*/
			client.addCachePool(poolInfo);
			System.out.println("成功创建CachePoolInfo为："+poolInfo.toString()+"的cachePool！");
			
			/*修改CachePool的配置信息*/
			poolInfo.setOwnerName(newOwner);
			client.modifyCachePool(poolInfo);
			System.out.println("成功修改CachePoolInfo-->OwnerName为："+poolInfo.getOwnerName()+"!");
			
			/*罗列CachePool*/
			RemoteIterator<CachePoolEntry> iter=client.listCachePools();
			while (iter.hasNext()) {
				System.out.println("CachePoolInfo为："+iter.next().getInfo().toString());
			}
			
			/*删除CachePool*/
			client.removeCachePool(pool);
			System.out.println("删除以后的CachePoolInfo：");
			RemoteIterator<CachePoolEntry> iterator=client.listCachePools();
			while (iterator.hasNext()) {
				System.out.println("CachePoolInfo为："+iterator.next().getInfo().toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*StoragePolicy的get、set方法设置*/
	public static void StoragePolicyTest(DFSClient client,String path,String policyType) {
		try {
			BlockStoragePolicy[] policies = client.getStoragePolicies();
			for (int i = 0; i < policies.length; i++) {
				System.out.println(policies[i].toString());
			}
			
			/*终端查询以后不成功,api验证成功！*/
			client.setStoragePolicy(path, policyType);
			
			/*使用getFileInfo()-->getStoragePolicy()获取文件的storagePolicy*/
			HdfsFileStatus status=client.getFileInfo("/test/11.txt");
			byte policy=status.getStoragePolicy();
			
			/*将byte转化为int类型*/
			int id=policy&0xff;
			/*查询id对应的storagePolicy类型*/
			switch (id) {
			case 2:
				System.out.println(path+"的StoragePolicy为：COLD");
				break;
			case 5:
				System.out.println(path+"的StoragePolicy为：WARM");
				break;
			case 7:
				System.out.println(path+"的StoragePolicy为：HOT");
				break;
			case 10:
				System.out.println(path+"的StoragePolicy为：ONE_SSD");
				break;
			case 12:
				System.out.println(path+"的StoragePolicy为：ALL_SSD");
				break;
			case 15:
				System.out.println(path+"的StoragePolicy为：LAZY_PERSIST");
				break;
			default:
				System.out.println(path+"的StoragePolicy识别失败！");
				break;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*快照相关操作测试*/
	public static void SnapshotTest(DFSClient client,String snapshotRoot,
			String snapshotName) {
		try {
			//为snapshot创建专门的目录
			client.mkdirs(snapshotRoot);
			
			//设置允许创建快照的目录snapshotRoot
			client.allowSnapshot(snapshotRoot);
			System.out.println("设置允许创建快照的目录"+snapshotRoot+"成功！");
			
			//创建名为snapshotName的快照
			client.createSnapshot(snapshotRoot,snapshotName);
			client.createSnapshot(snapshotRoot,snapshotName+"2");
			System.out.println("创建快照"+snapshotName+"成功！");
			
			//获取快照目录的相关信息,如每个快照目录下已经创建的快照数、允许创建的快照数
			SnapshottableDirectoryStatus[] list=client.getSnapshottableDirListing();
			for(int i=0;i<list.length;i++){
				System.out.println("snapshotNumber："+list[i].getSnapshotNumber());
				System.out.println("snapshotQuota："+list[i].getSnapshotQuota());
			}
			
			//重命名快照
			client.renameSnapshot(snapshotRoot,snapshotName,snapshotName+"1");
			System.out.println("重命名快照"+snapshotName+"为："+snapshotName+"1");
            
			//删除快照
			client.deleteSnapshot(snapshotRoot, snapshotName+"1");
			System.out.println("删除快照"+snapshotName+"1成功！");
			
			//不允许创建快照的目录
			/*client.disallowSnapshot(snapshotRoot);
			client.createSnapshot(snapshotRoot,snapshotName);*/
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*获取DatanodeStorageReport，包括dataNode的DatanodeInfo及其每块磁盘的StorageReport*/
	public static void getDatanodeStorageReportTest(DFSClient client) {
		try {
			DatanodeStorageReport[] report = client.getDatanodeStorageReport(
					DatanodeReportType.ALL);
			for(int i=0;i<report.length;i++){
				/*通过DatanodeStorageReport获取DatanodeInfo*/
				DatanodeInfo info=report[i].getDatanodeInfo();
				System.out.println("DatanodeInfo："+info.getDatanodeReport());
				/*获取dataNode上每块磁盘的StorageReport？*/
				/*StorageReport[] storage=report[i].getStorageReports();
				for(int j=0;j<storage.length;j++){
					System.out.println(storage[j].getStorage().toString());
					System.out.println(storage[j].getCapacity()/1024.0/1024.0/1024.0);
				}*/
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*获取DatanodeInfo，包括dataNode的Name、Hostname、Configured Capacity等*/
	public static void getDatanodeReport(DFSClient client) {
		try {
			DatanodeInfo[] infos = client.datanodeReport(DatanodeReportType.ALL);
			//循环遍历所有的dataNode
			for(int i=0;i<infos.length;i++){
				System.out.println("Name: "+infos[i].getName());
				System.out.println("Hostname: "+infos[i].getHostName());
				System.out.println("Configured Capacity: "+infos[i].getCapacity());
				System.out.println("DFS Used: "+infos[i].getDfsUsed());
				System.out.println("*******************************************");	
				/*使用一个函数获取所有DatanodeInfo*/
				System.out.println(infos[i].getDatanodeReport());	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*测试集群的磁盘状态*/
	public static void DiskStatusTest(DFSClient client) {
		try {
			FsStatus fdisk = client.getDiskStatus();
			System.out.println("磁盘容量（G）："+fdisk.getCapacity()/1024.0/1024.0/1024.0);
			System.out.println("磁盘已使用（G）："+fdisk.getUsed()/1024.0/1024.0/1024.0);
			System.out.println("磁盘剩余（G）："+fdisk.getRemaining()/1024.0/1024.0/1024.0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	 /*测试HdfsFileStatus*/
	  public static void HdfsFileStatusTest(DFSClient client,String file){
				try {
					HdfsFileStatus status = client.getFileInfo(file);
					System.out.println(file+"的文件长度为："+status.getLen());
					System.out.println(file+"是一个目录："+status.isDir());
					System.out.println(file+"是Symlink："+status.isSymlink());
					System.out.println(file+"的文件块大小："+status.getBlockSize());
					System.out.println(file+"的副本数："+status.getReplication());
					System.out.println(file+"的修改时间："+(new Date(status.getModificationTime())));
					System.out.println(file+"的访问时间："+(new Date(status.getAccessTime())));
					System.out.println(file+"的权限："+status.getPermission().toString());
					System.out.println(file+"的所有者："+status.getOwner());
					System.out.println(file+"的组："+status.getGroup());
					System.out.println(file+"local名字为空："+status.isEmptyLocalName());
					System.out.println(file+"的local名字："+status.getLocalName());
					System.out.println(file+"的full名字："+status.getFullName("/test"));
					System.out.println(file+"的full Path："+status.getFullPath(new Path("/test")));
					//System.out.println(file+"的Symlink："+status.getSymlink());
					System.out.println(file+"的FileId："+status.getFileId());
					System.out.println(file+"的ChildrenNum："+status.getChildrenNum());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
	  }
	  
	  /* 文件副本数测试,设置文件副本数并获取文件副本数信息。
	   * 发现HdfsFileStatus其实就是文件的基本元数据信息*/
	  public static void ReplicaTest(DFSClient client,String file) {
		  try {
			//设置文件副本数
			client.setReplication(file, (short)2);
			//获取文件副本数信息
			HdfsFileStatus status=client.getFileInfo(file);
			System.out.println(file+"的副本数："+status.getReplication());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	  /*文件上传*/
	  public static void UploadFile(DFSClient client,String src,String dst) {
		  int byteBuffer=0;
	      byte[] buffer=new byte[1024];
	      
	      try {
				OutputStream out= client.create(dst,true);
				client.setPermission(dst, new FsPermission(
						FsAction.ALL,FsAction.ALL,FsAction.ALL));
				BufferedInputStream in = new BufferedInputStream(new 
		      		FileInputStream(src));
				
				while((byteBuffer=in.read(buffer))!=-1){
					 //out.write(buffer); 采用此方法在读取文件末尾数据时，不是写入的读入字节的长度.除非文件大小刚好被BUFFER_SIZE整除
					out.write(buffer, 0, byteBuffer);//指定每次写入的数据长度为实际读取的数据长度
					System.out.println(byteBuffer);
					}
				
				out.flush();
				out.close();
				in.close();
				System.out.println("文件上传完成！");
				}catch (IOException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
	      }
	  
	  /*文件追加写*/
	  public static void AppendFile(DFSClient client,String src,String dst) {
		  //首先检查文件是否存在
		  try {
			//不存在，先创建文件
			if (!client.exists(dst)) {
				OutputStream creat=client.create(dst,true);
				client.setPermission(dst, new FsPermission(FsAction.ALL,
						FsAction.ALL,FsAction.ALL));
				creat.close();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  int byteBuffer=0;
		  byte[] buffer=new byte[1024];
		  try {
			  HdfsDataOutputStream out=client.append(dst, 1024, 
			   		   EnumSet.of(CreateFlag.APPEND), new Progressable() {
			           @Override
			           public void progress() {}
			           }, null);
			  BufferedInputStream in = new BufferedInputStream(new 
			      		FileInputStream(src));
			  
			  while ((byteBuffer = in.read(buffer)) !=-1){
			   	  System.out.println(byteBuffer);
			   	  out.write(buffer,0,byteBuffer);
			  }
			  
			  out.flush();
			  out.close();
			  in.close();
			  System.out.println("文件追加写完成！");
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}   
	}
	  
	  /*文件下载*/
	  public static void DownloadFile(DFSClient client,String src,String dst) {
		  int byteBuffer=0;
		  byte[] buffer=new byte[1024];
		  try{
			  DFSInputStream in=client.open(src);
		      BufferedOutputStream out = new BufferedOutputStream(new 
		      		FileOutputStream(dst));
		      while ((byteBuffer = in.read(buffer)) !=-1)
		      {
		    	  System.out.println(byteBuffer);
		    	  out.write(buffer,0,byteBuffer);//指定每次写入的数据长度为实际读取的数据长度
		      }
		      
		      out.flush();
		      out.close();
		      in.close();
		      System.out.println("文件已下载！");
		  }catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}      
	}

}
