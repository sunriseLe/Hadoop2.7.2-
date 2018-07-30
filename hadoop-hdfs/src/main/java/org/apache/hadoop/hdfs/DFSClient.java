/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs;

import static org.apache.hadoop.fs.CommonConfigurationKeysPublic.HADOOP_SECURITY_CRYPTO_CODEC_CLASSES_KEY_PREFIX;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_BLOCK_SIZE_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_BLOCK_SIZE_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_BYTES_PER_CHECKSUM_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_BYTES_PER_CHECKSUM_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_BLOCK_WRITE_LOCATEFOLLOWINGBLOCK_RETRIES_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_BLOCK_WRITE_LOCATEFOLLOWINGBLOCK_RETRIES_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_BLOCK_WRITE_RETRIES_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_BLOCK_WRITE_RETRIES_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_CACHED_CONN_RETRY_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_CACHED_CONN_RETRY_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_CACHE_DROP_BEHIND_READS;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_CACHE_DROP_BEHIND_WRITES;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_CACHE_READAHEAD;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_CONTEXT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_CONTEXT_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_DATANODE_RESTART_TIMEOUT_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_DATANODE_RESTART_TIMEOUT_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_FAILOVER_MAX_ATTEMPTS_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_FAILOVER_MAX_ATTEMPTS_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_FAILOVER_SLEEPTIME_BASE_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_FAILOVER_SLEEPTIME_BASE_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_FAILOVER_SLEEPTIME_MAX_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_FAILOVER_SLEEPTIME_MAX_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_MAX_BLOCK_ACQUIRE_FAILURES_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_MAX_BLOCK_ACQUIRE_FAILURES_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_READ_PREFETCH_SIZE_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_RETRY_MAX_ATTEMPTS_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_RETRY_MAX_ATTEMPTS_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_RETRY_WINDOW_BASE;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_SOCKET_CACHE_CAPACITY_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_SOCKET_CACHE_CAPACITY_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_SOCKET_CACHE_EXPIRY_MSEC_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_SOCKET_CACHE_EXPIRY_MSEC_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_SOCKET_TIMEOUT_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_USE_DN_HOSTNAME;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_USE_DN_HOSTNAME_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_WRITE_EXCLUDE_NODES_CACHE_EXPIRY_INTERVAL;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_WRITE_EXCLUDE_NODES_CACHE_EXPIRY_INTERVAL_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_WRITE_PACKET_SIZE_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_CLIENT_WRITE_PACKET_SIZE_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_DATANODE_SOCKET_WRITE_TIMEOUT_KEY;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_REPLICATION_DEFAULT;
import static org.apache.hadoop.hdfs.DFSConfigKeys.DFS_REPLICATION_KEY;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.acl.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.SocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.directory.api.util.ByteBuffer;
import org.apache.hadoop.HadoopIllegalArgumentException;
import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.crypto.CipherSuite;
import org.apache.hadoop.crypto.CryptoCodec;
import org.apache.hadoop.crypto.CryptoInputStream;
import org.apache.hadoop.crypto.CryptoOutputStream;
import org.apache.hadoop.crypto.CryptoProtocolVersion;
import org.apache.hadoop.crypto.key.KeyProvider;
import org.apache.hadoop.crypto.key.KeyProvider.KeyVersion;
import org.apache.hadoop.crypto.key.KeyProviderCryptoExtension;
import org.apache.hadoop.crypto.key.KeyProviderCryptoExtension.EncryptedKeyVersion;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.BlockStorageLocation;
import org.apache.hadoop.fs.CacheFlag;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.CreateFlag;
import org.apache.hadoop.fs.FileAlreadyExistsException;
import org.apache.hadoop.fs.FileEncryptionInfo;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsServerDefaults;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.fs.HdfsBlockLocation;
import org.apache.hadoop.fs.InvalidPathException;
import org.apache.hadoop.fs.MD5MD5CRC32CastagnoliFileChecksum;
import org.apache.hadoop.fs.MD5MD5CRC32FileChecksum;
import org.apache.hadoop.fs.MD5MD5CRC32GzipFileChecksum;
import org.apache.hadoop.fs.Options;
import org.apache.hadoop.fs.Options.ChecksumOpt;
import org.apache.hadoop.fs.ParentNotDirectoryException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.StorageType;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.UnresolvedLinkException;
import org.apache.hadoop.fs.VolumeId;
import org.apache.hadoop.fs.XAttr;
import org.apache.hadoop.fs.XAttrSetFlag;
import org.apache.hadoop.fs.permission.AclEntry;
import org.apache.hadoop.fs.permission.AclStatus;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.hdfs.client.HdfsDataInputStream;
import org.apache.hadoop.hdfs.client.HdfsDataOutputStream;
import org.apache.hadoop.hdfs.net.Peer;
import org.apache.hadoop.hdfs.net.TcpPeerServer;
import org.apache.hadoop.hdfs.protocol.AclException;
import org.apache.hadoop.hdfs.protocol.BlockStoragePolicy;
import org.apache.hadoop.hdfs.protocol.CacheDirectiveEntry;
import org.apache.hadoop.hdfs.protocol.CacheDirectiveInfo;
import org.apache.hadoop.hdfs.protocol.CacheDirectiveIterator;
import org.apache.hadoop.hdfs.protocol.CachePoolEntry;
import org.apache.hadoop.hdfs.protocol.CachePoolInfo;
import org.apache.hadoop.hdfs.protocol.CachePoolIterator;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.hdfs.protocol.CorruptFileBlocks;
import org.apache.hadoop.hdfs.protocol.DSQuotaExceededException;
import org.apache.hadoop.hdfs.protocol.DatanodeID;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.DirectoryListing;
import org.apache.hadoop.hdfs.protocol.EncryptionZone;
import org.apache.hadoop.hdfs.protocol.EncryptionZoneIterator;
import org.apache.hadoop.hdfs.protocol.ExtendedBlock;
import org.apache.hadoop.hdfs.protocol.HdfsBlocksMetadata;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.DatanodeReportType;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.RollingUpgradeAction;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.SafeModeAction;
import org.apache.hadoop.hdfs.protocol.HdfsFileStatus;
import org.apache.hadoop.hdfs.protocol.LastBlockWithStatus;
import org.apache.hadoop.hdfs.protocol.LocatedBlock;
import org.apache.hadoop.hdfs.protocol.LocatedBlocks;
import org.apache.hadoop.hdfs.protocol.NSQuotaExceededException;
import org.apache.hadoop.hdfs.protocol.QuotaByStorageTypeExceededException;
import org.apache.hadoop.hdfs.protocol.RollingUpgradeInfo;
import org.apache.hadoop.hdfs.protocol.SnapshotAccessControlException;
import org.apache.hadoop.hdfs.protocol.SnapshotDiffReport;
import org.apache.hadoop.hdfs.protocol.SnapshottableDirectoryStatus;
import org.apache.hadoop.hdfs.protocol.UnresolvedPathException;
import org.apache.hadoop.hdfs.protocol.datatransfer.DataTransferProtoUtil;
import org.apache.hadoop.hdfs.protocol.datatransfer.IOStreamPair;
import org.apache.hadoop.hdfs.protocol.datatransfer.Op;
import org.apache.hadoop.hdfs.protocol.datatransfer.ReplaceDatanodeOnFailure;
import org.apache.hadoop.hdfs.protocol.datatransfer.Sender;
import org.apache.hadoop.hdfs.protocol.datatransfer.TrustedChannelResolver;
import org.apache.hadoop.hdfs.protocol.datatransfer.sasl.DataEncryptionKeyFactory;
import org.apache.hadoop.hdfs.protocol.datatransfer.sasl.DataTransferSaslUtil;
import org.apache.hadoop.hdfs.protocol.datatransfer.sasl.SaslDataTransferClient;
import org.apache.hadoop.hdfs.protocol.proto.DataTransferProtos.BlockOpResponseProto;
import org.apache.hadoop.hdfs.protocol.proto.DataTransferProtos.OpBlockChecksumResponseProto;
import org.apache.hadoop.hdfs.protocol.proto.DataTransferProtos.Status;
import org.apache.hadoop.hdfs.protocolPB.PBHelper;
import org.apache.hadoop.hdfs.security.token.block.BlockTokenIdentifier;
import org.apache.hadoop.hdfs.security.token.block.DataEncryptionKey;
import org.apache.hadoop.hdfs.security.token.block.InvalidBlockTokenException;
import org.apache.hadoop.hdfs.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.hdfs.server.common.HdfsServerConstants;
import org.apache.hadoop.hdfs.server.datanode.CachingStrategy;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.hdfs.server.namenode.SafeModeException;
import org.apache.hadoop.hdfs.server.protocol.DatanodeStorageReport;
import org.apache.hadoop.hdfs.util.ByteArrayManager;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.EnumSetWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.retry.LossyRetryInvocationHandler;
import org.apache.hadoop.ipc.Client;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.ipc.RpcInvocationHandler;
import org.apache.hadoop.net.DNS;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.token.SecretManager.InvalidToken;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.security.token.TokenRenewer;
import org.apache.hadoop.tracing.SpanReceiverHost;
import org.apache.hadoop.tracing.TraceUtils;
import org.apache.hadoop.util.Daemon;
import org.apache.hadoop.util.DataChecksum;
import org.apache.hadoop.util.DataChecksum.Type;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.Time;
import org.apache.htrace.Sampler;
import org.apache.htrace.SamplerBuilder;
import org.apache.htrace.Span;
import org.apache.htrace.Trace;
import org.apache.htrace.TraceScope;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;
import com.sun.tools.doclint.Checker.Flag;

import javafx.util.converter.PercentageStringConverter;
import sun.security.acl.PermissionImpl;

/********************************************************
 * DFSClient can connect to a Hadoop Filesystem and 
 * perform basic file tasks.  It uses the ClientProtocol
 * to communicate with a NameNode daemon, and connects 
 * directly to DataNodes to read/write block data.
 *
 * Hadoop DFS users should obtain an instance of 
 * DistributedFileSystem, which uses DFSClient to handle
 * filesystem tasks.
 *
 ********************************************************/
@InterfaceAudience.Private
public class DFSClient implements java.io.Closeable, RemotePeerFactory,
    DataEncryptionKeyFactory {
	//LogFactory.getLog()则来自apache的common-logging包 
  public static final Log LOG = LogFactory.getLog(DFSClient.class);
  //1 hour，会话时间限制
  public static final long SERVER_DEFAULTS_VALIDITY_PERIOD = 60 * 60 * 1000L; // 1 hour
  static final int TCP_WINDOW_SIZE = 128 * 1024; // 128 KB

  private final Configuration conf;
  private final Conf dfsClientConf;//dfs用户端的配置
  final ClientProtocol namenode;
  /* The service used for delegation tokens */
  private Text dtService;

  final UserGroupInformation ugi;//文件系统的用户组信息
  volatile boolean clientRunning = true;
  volatile long lastLeaseRenewal;
  private volatile FsServerDefaults serverDefaults;
  private volatile long serverDefaultsLastUpdate;
  final String clientName;
  final SocketFactory socketFactory;
  final ReplaceDatanodeOnFailure dtpReplaceDatanodeOnFailure;
  final FileSystem.Statistics stats;//文件系统stats
  private final String authority;//文件系统URI中的授权信息
  private final Random r = new Random();
  private SocketAddress[] localInterfaceAddrs;
  private DataEncryptionKey encryptionKey;//数据加密秘钥
  final SaslDataTransferClient saslClient;
  private final CachingStrategy defaultReadCachingStrategy;//默认的读缓存策略
  private final CachingStrategy defaultWriteCachingStrategy;//默认的写缓存策略
  private final ClientContext clientContext;
  private volatile long hedgedReadThresholdMillis;
  private static final DFSHedgedReadMetrics HEDGED_READ_METRIC =
      new DFSHedgedReadMetrics();//客户端hedged read feature的metrics
  private static ThreadPoolExecutor HEDGED_READ_THREAD_POOL;
  private final Sampler<?> traceSampler;

  /**
   * DFSClient configuration 
   */
  public static class Conf {
    final int hdfsTimeout;    // timeout value for a DFS operation.

    final int maxFailoverAttempts;//最大的故障转移尝试次数
    final int maxRetryAttempts;//重新尝试的最大次数
    final int failoverSleepBaseMillis;
    final int failoverSleepMaxMillis;
    final int maxBlockAcquireFailures;//能允许的最大块故障数
    final int confTime;
    final int ioBufferSize;
    final ChecksumOpt defaultChecksumOpt;
    final int writePacketSize;//写操作的包大小
    final int writeMaxPackets;//写操作最大的包数量
    final ByteArrayManager.Conf writeByteArrayManagerConf;
    final int socketTimeout;
    final int socketCacheCapacity;
    final long socketCacheExpiry;//socket缓存到期时间？
    final long excludedNodesCacheExpiry;//排除节点缓存过期？
    /** Wait time window (in msec) if BlockMissingException is caught */
    final int timeWindow;//等待的时间窗，如果快确实异常被捕获
    final int nCachedConnRetry;
    final int nBlockWriteRetry;
    final int nBlockWriteLocateFollowingRetry;
    final long defaultBlockSize;
    final long prefetchSize;//预取大小
    final short defaultReplication;
    final String taskId;//任务ID
    final FsPermission uMask;
    final boolean connectToDnViaHostname;
    final boolean getHdfsBlocksMetadataEnabled;//允许获取数据块元数据？
    final int getFileBlockStorageLocationsNumThreads;
    final int getFileBlockStorageLocationsTimeoutMs;//获取文件块存储位置的超时设置，单位ms？
    final int retryTimesForGetLastBlockLength;
    final int retryIntervalForGetLastBlockLength;
    final long datanodeRestartTimeout;//数据节点重启超时设置
    final long dfsclientSlowIoWarningThresholdMs;//缓慢io的阈值？
   
    final boolean useLegacyBlockReader;//使用旧版块阅读器
    final boolean useLegacyBlockReaderLocal;
    final String domainSocketPath;
    final boolean skipShortCircuitChecksums;
    final int shortCircuitBufferSize;//短路缓冲器大小？
    final boolean shortCircuitLocalReads;
    final boolean domainSocketDataTraffic;//域套接字数据流量
    final int shortCircuitStreamsCacheSize;
    final long shortCircuitStreamsCacheExpiryMs;//短路流式缓存过期时间，单位ms？ 
    final int shortCircuitSharedMemoryWatcherInterruptCheckMs;
    
    final boolean shortCircuitMmapEnabled;
    final int shortCircuitMmapCacheSize;
    final long shortCircuitMmapCacheExpiryMs;
    final long shortCircuitMmapCacheRetryTimeout;
    final long shortCircuitCacheStaleThresholdMs;

    final long keyProviderCacheExpiryMs;
    public BlockReaderFactory.FailureInjector brfFailureInjector =
      new BlockReaderFactory.FailureInjector();
    
    //构造函数，初始化配置参数；大部分的配置参数都是，通过key没有获取配置值，就返回默认值
    public Conf(Configuration conf) {
      // The hdfsTimeout is currently the same as the ipc timeout 
      hdfsTimeout = Client.getTimeout(conf);
      maxFailoverAttempts = conf.getInt(
          DFS_CLIENT_FAILOVER_MAX_ATTEMPTS_KEY,
          DFS_CLIENT_FAILOVER_MAX_ATTEMPTS_DEFAULT);
      maxRetryAttempts = conf.getInt(
          DFS_CLIENT_RETRY_MAX_ATTEMPTS_KEY,
          DFS_CLIENT_RETRY_MAX_ATTEMPTS_DEFAULT);
      failoverSleepBaseMillis = conf.getInt(//故障转移的休眠时间
          DFS_CLIENT_FAILOVER_SLEEPTIME_BASE_KEY,
          DFS_CLIENT_FAILOVER_SLEEPTIME_BASE_DEFAULT);
      failoverSleepMaxMillis = conf.getInt(//故障转移的最大休眠时间
          DFS_CLIENT_FAILOVER_SLEEPTIME_MAX_KEY,
          DFS_CLIENT_FAILOVER_SLEEPTIME_MAX_DEFAULT);

      maxBlockAcquireFailures = conf.getInt(
          DFS_CLIENT_MAX_BLOCK_ACQUIRE_FAILURES_KEY,
          DFS_CLIENT_MAX_BLOCK_ACQUIRE_FAILURES_DEFAULT);
      confTime = conf.getInt(DFS_DATANODE_SOCKET_WRITE_TIMEOUT_KEY,
          HdfsServerConstants.WRITE_TIMEOUT);
      ioBufferSize = conf.getInt(
          CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_KEY,
          CommonConfigurationKeysPublic.IO_FILE_BUFFER_SIZE_DEFAULT);
      defaultChecksumOpt = getChecksumOptFromConf(conf);
      socketTimeout = conf.getInt(DFS_CLIENT_SOCKET_TIMEOUT_KEY,
          HdfsServerConstants.READ_TIMEOUT);
      /** dfs.write.packet.size is an internal config variable */
      writePacketSize = conf.getInt(DFS_CLIENT_WRITE_PACKET_SIZE_KEY,
          DFS_CLIENT_WRITE_PACKET_SIZE_DEFAULT);
      writeMaxPackets = conf.getInt(
          DFSConfigKeys.DFS_CLIENT_WRITE_MAX_PACKETS_IN_FLIGHT_KEY,
          DFSConfigKeys.DFS_CLIENT_WRITE_MAX_PACKETS_IN_FLIGHT_DEFAULT);
      
      final boolean byteArrayManagerEnabled = conf.getBoolean(
          DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_ENABLED_KEY,
          DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_ENABLED_DEFAULT);
      if (!byteArrayManagerEnabled) {
        writeByteArrayManagerConf = null;
      } else {
        final int countThreshold = conf.getInt(
            DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_COUNT_THRESHOLD_KEY,
            DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_COUNT_THRESHOLD_DEFAULT);
        final int countLimit = conf.getInt(
            DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_COUNT_LIMIT_KEY,
            DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_COUNT_LIMIT_DEFAULT);
        final long countResetTimePeriodMs = conf.getLong(
            DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_COUNT_RESET_TIME_PERIOD_MS_KEY,
            DFSConfigKeys.DFS_CLIENT_WRITE_BYTE_ARRAY_MANAGER_COUNT_RESET_TIME_PERIOD_MS_DEFAULT);
        writeByteArrayManagerConf = new ByteArrayManager.Conf(
            countThreshold, countLimit, countResetTimePeriodMs); 
      }
      
      
      defaultBlockSize = conf.getLongBytes(DFS_BLOCK_SIZE_KEY,
          DFS_BLOCK_SIZE_DEFAULT);
      defaultReplication = (short) conf.getInt(//如果通过key没有配置副本数，就返回默认副本数
          DFS_REPLICATION_KEY, DFS_REPLICATION_DEFAULT);
      taskId = conf.get("mapreduce.task.attempt.id", "NONMAPREDUCE");
      socketCacheCapacity = conf.getInt(DFS_CLIENT_SOCKET_CACHE_CAPACITY_KEY,
          DFS_CLIENT_SOCKET_CACHE_CAPACITY_DEFAULT);
      socketCacheExpiry = conf.getLong(DFS_CLIENT_SOCKET_CACHE_EXPIRY_MSEC_KEY,
          DFS_CLIENT_SOCKET_CACHE_EXPIRY_MSEC_DEFAULT);
      excludedNodesCacheExpiry = conf.getLong(
          DFS_CLIENT_WRITE_EXCLUDE_NODES_CACHE_EXPIRY_INTERVAL,
          DFS_CLIENT_WRITE_EXCLUDE_NODES_CACHE_EXPIRY_INTERVAL_DEFAULT);
      prefetchSize = conf.getLong(DFS_CLIENT_READ_PREFETCH_SIZE_KEY,
          10 * defaultBlockSize);//预取大小默认为块大小的10倍
      timeWindow = conf.getInt(DFS_CLIENT_RETRY_WINDOW_BASE, 3000);//默认时间3s
      nCachedConnRetry = conf.getInt(DFS_CLIENT_CACHED_CONN_RETRY_KEY,
          DFS_CLIENT_CACHED_CONN_RETRY_DEFAULT);
      nBlockWriteRetry = conf.getInt(DFS_CLIENT_BLOCK_WRITE_RETRIES_KEY,
          DFS_CLIENT_BLOCK_WRITE_RETRIES_DEFAULT);
      nBlockWriteLocateFollowingRetry = conf.getInt(
          DFS_CLIENT_BLOCK_WRITE_LOCATEFOLLOWINGBLOCK_RETRIES_KEY,
          DFS_CLIENT_BLOCK_WRITE_LOCATEFOLLOWINGBLOCK_RETRIES_DEFAULT);
      uMask = FsPermission.getUMask(conf);
      connectToDnViaHostname = conf.getBoolean(DFS_CLIENT_USE_DN_HOSTNAME,
          DFS_CLIENT_USE_DN_HOSTNAME_DEFAULT);//通过hostname连接DN
      getHdfsBlocksMetadataEnabled = conf.getBoolean(
          DFSConfigKeys.DFS_HDFS_BLOCKS_METADATA_ENABLED, 
          DFSConfigKeys.DFS_HDFS_BLOCKS_METADATA_ENABLED_DEFAULT);
      getFileBlockStorageLocationsNumThreads = conf.getInt(//读取文件块存储位置的线程数
          DFSConfigKeys.DFS_CLIENT_FILE_BLOCK_STORAGE_LOCATIONS_NUM_THREADS,
          DFSConfigKeys.DFS_CLIENT_FILE_BLOCK_STORAGE_LOCATIONS_NUM_THREADS_DEFAULT);
      getFileBlockStorageLocationsTimeoutMs = conf.getInt(
          DFSConfigKeys.DFS_CLIENT_FILE_BLOCK_STORAGE_LOCATIONS_TIMEOUT_MS,
          DFSConfigKeys.DFS_CLIENT_FILE_BLOCK_STORAGE_LOCATIONS_TIMEOUT_MS_DEFAULT);
      retryTimesForGetLastBlockLength = conf.getInt(//获取最后一个块长度的重试时间？
          DFSConfigKeys.DFS_CLIENT_RETRY_TIMES_GET_LAST_BLOCK_LENGTH,
          DFSConfigKeys.DFS_CLIENT_RETRY_TIMES_GET_LAST_BLOCK_LENGTH_DEFAULT);
      retryIntervalForGetLastBlockLength = conf.getInt(
        DFSConfigKeys.DFS_CLIENT_RETRY_INTERVAL_GET_LAST_BLOCK_LENGTH,
        DFSConfigKeys.DFS_CLIENT_RETRY_INTERVAL_GET_LAST_BLOCK_LENGTH_DEFAULT);

      useLegacyBlockReader = conf.getBoolean(
          DFSConfigKeys.DFS_CLIENT_USE_LEGACY_BLOCKREADER,
          DFSConfigKeys.DFS_CLIENT_USE_LEGACY_BLOCKREADER_DEFAULT);
      useLegacyBlockReaderLocal = conf.getBoolean(
          DFSConfigKeys.DFS_CLIENT_USE_LEGACY_BLOCKREADERLOCAL,
          DFSConfigKeys.DFS_CLIENT_USE_LEGACY_BLOCKREADERLOCAL_DEFAULT);
      shortCircuitLocalReads = conf.getBoolean(
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_KEY,
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_DEFAULT);
      domainSocketDataTraffic = conf.getBoolean(
          DFSConfigKeys.DFS_CLIENT_DOMAIN_SOCKET_DATA_TRAFFIC,
          DFSConfigKeys.DFS_CLIENT_DOMAIN_SOCKET_DATA_TRAFFIC_DEFAULT);
      domainSocketPath = conf.getTrimmed(
          DFSConfigKeys.DFS_DOMAIN_SOCKET_PATH_KEY,
          DFSConfigKeys.DFS_DOMAIN_SOCKET_PATH_DEFAULT);

      if (BlockReaderLocal.LOG.isDebugEnabled()) {//是否允许调试？
        BlockReaderLocal.LOG.debug(
            DFSConfigKeys.DFS_CLIENT_USE_LEGACY_BLOCKREADERLOCAL
            + " = " + useLegacyBlockReaderLocal);
        BlockReaderLocal.LOG.debug(
            DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_KEY
            + " = " + shortCircuitLocalReads);
        BlockReaderLocal.LOG.debug(
            DFSConfigKeys.DFS_CLIENT_DOMAIN_SOCKET_DATA_TRAFFIC
            + " = " + domainSocketDataTraffic);
        BlockReaderLocal.LOG.debug(
            DFSConfigKeys.DFS_DOMAIN_SOCKET_PATH_KEY
            + " = " + domainSocketPath);
      }

      skipShortCircuitChecksums = conf.getBoolean(
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_SKIP_CHECKSUM_KEY,
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_SKIP_CHECKSUM_DEFAULT);
      shortCircuitBufferSize = conf.getInt(
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_BUFFER_SIZE_KEY,
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_BUFFER_SIZE_DEFAULT);
      shortCircuitStreamsCacheSize = conf.getInt(
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_STREAMS_CACHE_SIZE_KEY,
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_STREAMS_CACHE_SIZE_DEFAULT);
      shortCircuitStreamsCacheExpiryMs = conf.getLong(
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_STREAMS_CACHE_EXPIRY_MS_KEY,
          DFSConfigKeys.DFS_CLIENT_READ_SHORTCIRCUIT_STREAMS_CACHE_EXPIRY_MS_DEFAULT);
      shortCircuitMmapEnabled = conf.getBoolean(
          DFSConfigKeys.DFS_CLIENT_MMAP_ENABLED,
          DFSConfigKeys.DFS_CLIENT_MMAP_ENABLED_DEFAULT);
      shortCircuitMmapCacheSize = conf.getInt(
          DFSConfigKeys.DFS_CLIENT_MMAP_CACHE_SIZE,
          DFSConfigKeys.DFS_CLIENT_MMAP_CACHE_SIZE_DEFAULT);
      shortCircuitMmapCacheExpiryMs = conf.getLong(
          DFSConfigKeys.DFS_CLIENT_MMAP_CACHE_TIMEOUT_MS,
          DFSConfigKeys.DFS_CLIENT_MMAP_CACHE_TIMEOUT_MS_DEFAULT);
      shortCircuitMmapCacheRetryTimeout = conf.getLong(
          DFSConfigKeys.DFS_CLIENT_MMAP_RETRY_TIMEOUT_MS,
          DFSConfigKeys.DFS_CLIENT_MMAP_RETRY_TIMEOUT_MS_DEFAULT);
      shortCircuitCacheStaleThresholdMs = conf.getLong(
          DFSConfigKeys.DFS_CLIENT_SHORT_CIRCUIT_REPLICA_STALE_THRESHOLD_MS,
          DFSConfigKeys.DFS_CLIENT_SHORT_CIRCUIT_REPLICA_STALE_THRESHOLD_MS_DEFAULT);
      shortCircuitSharedMemoryWatcherInterruptCheckMs = conf.getInt(
          DFSConfigKeys.DFS_SHORT_CIRCUIT_SHARED_MEMORY_WATCHER_INTERRUPT_CHECK_MS,
          DFSConfigKeys.DFS_SHORT_CIRCUIT_SHARED_MEMORY_WATCHER_INTERRUPT_CHECK_MS_DEFAULT);

      datanodeRestartTimeout = conf.getLong(
          DFS_CLIENT_DATANODE_RESTART_TIMEOUT_KEY,
          DFS_CLIENT_DATANODE_RESTART_TIMEOUT_DEFAULT) * 1000;//读取出来的时间要乘以1000
      dfsclientSlowIoWarningThresholdMs = conf.getLong(
          DFSConfigKeys.DFS_CLIENT_SLOW_IO_WARNING_THRESHOLD_KEY,
          DFSConfigKeys.DFS_CLIENT_SLOW_IO_WARNING_THRESHOLD_DEFAULT);

      keyProviderCacheExpiryMs = conf.getLong(
          DFSConfigKeys.DFS_CLIENT_KEY_PROVIDER_CACHE_EXPIRY_MS,
          DFSConfigKeys.DFS_CLIENT_KEY_PROVIDER_CACHE_EXPIRY_DEFAULT);
    }

    public boolean isUseLegacyBlockReaderLocal() {
      return useLegacyBlockReaderLocal;
    }

    public String getDomainSocketPath() {
      return domainSocketPath;
    }

    public boolean isShortCircuitLocalReads() {
      return shortCircuitLocalReads;
    }

    public boolean isDomainSocketDataTraffic() {
      return domainSocketDataTraffic;
    }

    //获取数据校验和类型
    private DataChecksum.Type getChecksumType(Configuration conf) {
      final String checksum = conf.get(
          DFSConfigKeys.DFS_CHECKSUM_TYPE_KEY,
          DFSConfigKeys.DFS_CHECKSUM_TYPE_DEFAULT);
      try {
        return DataChecksum.Type.valueOf(checksum);
      } catch(IllegalArgumentException iae) {//出现异常，则使用默认校验和类型
        LOG.warn("Bad checksum type: " + checksum + ". Using default "
            + DFSConfigKeys.DFS_CHECKSUM_TYPE_DEFAULT);
        return DataChecksum.Type.valueOf(
            DFSConfigKeys.DFS_CHECKSUM_TYPE_DEFAULT); 
      }
    }

    // Construct a checksum option from conf
    private ChecksumOpt getChecksumOptFromConf(Configuration conf) {
      DataChecksum.Type type = getChecksumType(conf);
      int bytesPerChecksum = conf.getInt(DFS_BYTES_PER_CHECKSUM_KEY,//每次进行校验和的字节数
          DFS_BYTES_PER_CHECKSUM_DEFAULT);
      return new ChecksumOpt(type, bytesPerChecksum);
    }

    // create a DataChecksum with the default option.
    private DataChecksum createChecksum() throws IOException {
      return createChecksum(null);
    }

    //创建用户自定义操作的校验和？
    private DataChecksum createChecksum(ChecksumOpt userOpt) {
      // Fill in any missing field with the default.
      ChecksumOpt myOpt = ChecksumOpt.processChecksumOpt(
          defaultChecksumOpt, userOpt);
      DataChecksum dataChecksum = DataChecksum.newDataChecksum(
          myOpt.getChecksumType(),
          myOpt.getBytesPerChecksum());
      if (dataChecksum == null) {
        throw new HadoopIllegalArgumentException("Invalid checksum type: userOpt="
            + userOpt + ", default=" + defaultChecksumOpt
            + ", effective=null");
      }
      return dataChecksum;
    }
  }
 
  //获取hdfs的配置信息
  public Conf getConf() {
    return dfsClientConf;
  }

  Configuration getConfiguration() {
    return conf;
  }

  /**
   * A map from file names to {@link DFSOutputStream} objects
   * that are currently being written by this client.
   * 从文件名到正在被client写对象的映射，一个文件只能被一个用户写
   * Note that a file can only be written by a single client.
   */
  private final Map<Long, DFSOutputStream> filesBeingWritten
      = new HashMap<Long, DFSOutputStream>();

  /**
   * Same as this(NameNode.getAddress(conf), conf);
   * @see #DFSClient(InetSocketAddress, Configuration)
   * @deprecated Deprecated at 0.21
   */
  @Deprecated
  public DFSClient(Configuration conf) throws IOException {
    this(NameNode.getAddress(conf), conf);
  }
  
  //DFSClient的构造函数
  public DFSClient(InetSocketAddress address, Configuration conf) throws IOException {
    this(NameNode.getUri(address), conf);
  }

  /**
   * Same as this(nameNodeUri, conf, null);
   * @see #DFSClient(URI, Configuration, FileSystem.Statistics)
   */
  public DFSClient(URI nameNodeUri, Configuration conf
      ) throws IOException {
    this(nameNodeUri, conf, null);
  }

  /**
   * Same as this(nameNodeUri, null, conf, stats);
   * @see #DFSClient(URI, ClientProtocol, Configuration, FileSystem.Statistics) 
   */
  public DFSClient(URI nameNodeUri, Configuration conf,
                   FileSystem.Statistics stats)
    throws IOException {
    this(nameNodeUri, null, conf, stats);
  }
  
  /** 
   * Create a new DFSClient connected to the given nameNodeUri or rpcNamenode.
   * 创建dfs client与NN URI或者rpcNamenode的连接
   * If HA is enabled and a positive value is set for （高可用性）
   * {@link DFSConfigKeys#DFS_CLIENT_TEST_DROP_NAMENODE_RESPONSE_NUM_KEY} in the
   * configuration, the DFSClient will use {@link LossyRetryInvocationHandler}
   * as its RetryInvocationHandler. Otherwise one of nameNodeUri or rpcNamenode 
   * must be null.
   */
  @VisibleForTesting
  public DFSClient(URI nameNodeUri, ClientProtocol rpcNamenode,
      Configuration conf, FileSystem.Statistics stats)
    throws IOException {
    SpanReceiverHost.get(conf, DFSConfigKeys.DFS_CLIENT_HTRACE_PREFIX);
    traceSampler = new SamplerBuilder(TraceUtils.
        wrapHadoopConf(DFSConfigKeys.DFS_CLIENT_HTRACE_PREFIX, conf)).build();
    // Copy only the required DFSClient configuration
    this.dfsClientConf = new Conf(conf);
    if (this.dfsClientConf.useLegacyBlockReaderLocal) {
      LOG.debug("Using legacy short-circuit local reads.");
    }
    this.conf = conf;
    this.stats = stats;
    this.socketFactory = NetUtils.getSocketFactory(conf, ClientProtocol.class);
    this.dtpReplaceDatanodeOnFailure = ReplaceDatanodeOnFailure.get(conf);

    this.ugi = UserGroupInformation.getCurrentUser();
    
    this.authority = nameNodeUri == null? "null": nameNodeUri.getAuthority();
    this.clientName = "DFSClient_" + dfsClientConf.taskId + "_" + 
        DFSUtil.getRandom().nextInt()  + "_" + Thread.currentThread().getId();
    int numResponseToDrop = conf.getInt(
        DFSConfigKeys.DFS_CLIENT_TEST_DROP_NAMENODE_RESPONSE_NUM_KEY,
        DFSConfigKeys.DFS_CLIENT_TEST_DROP_NAMENODE_RESPONSE_NUM_DEFAULT);
    NameNodeProxies.ProxyAndInfo<ClientProtocol> proxyInfo = null;
    AtomicBoolean nnFallbackToSimpleAuth = new AtomicBoolean(false);
    if (numResponseToDrop > 0) {
      // This case is used for testing.
      LOG.warn(DFSConfigKeys.DFS_CLIENT_TEST_DROP_NAMENODE_RESPONSE_NUM_KEY
          + " is set to " + numResponseToDrop
          + ", this hacked client will proactively drop responses");//这个被黑客攻击的客户端会主动放弃响应 
      proxyInfo = NameNodeProxies.createProxyWithLossyRetryHandler(conf,
          nameNodeUri, ClientProtocol.class, numResponseToDrop,
          nnFallbackToSimpleAuth);
    }
    
    if (proxyInfo != null) {
      this.dtService = proxyInfo.getDelegationTokenService();
      this.namenode = proxyInfo.getProxy();
    } else if (rpcNamenode != null) {
      // This case is used for testing.
      Preconditions.checkArgument(nameNodeUri == null);
      this.namenode = rpcNamenode;
      dtService = null;
    } else {
      Preconditions.checkArgument(nameNodeUri != null,
          "null URI");
      proxyInfo = NameNodeProxies.createProxy(conf, nameNodeUri,
          ClientProtocol.class, nnFallbackToSimpleAuth);
      this.dtService = proxyInfo.getDelegationTokenService();
      this.namenode = proxyInfo.getProxy();
    }

    String localInterfaces[] =
      conf.getTrimmedStrings(DFSConfigKeys.DFS_CLIENT_LOCAL_INTERFACES);
    localInterfaceAddrs = getLocalInterfaceAddrs(localInterfaces);
    if (LOG.isDebugEnabled() && 0 != localInterfaces.length) {//打印debug信息？
      LOG.debug("Using local interfaces [" +
      Joiner.on(',').join(localInterfaces)+ "] with addresses [" +
      Joiner.on(',').join(localInterfaceAddrs) + "]");
    }
    
  //读取数据后，是否立即从操作系统缓冲区中删除
    Boolean readDropBehind = (conf.get(DFS_CLIENT_CACHE_DROP_BEHIND_READS) == null) ?
        null : conf.getBoolean(DFS_CLIENT_CACHE_DROP_BEHIND_READS, false);
    //预读取
    Long readahead = (conf.get(DFS_CLIENT_CACHE_READAHEAD) == null) ?
        null : conf.getLong(DFS_CLIENT_CACHE_READAHEAD, 0);
    //写数据后，是否立即从操作系统缓冲区中删除
    Boolean writeDropBehind = (conf.get(DFS_CLIENT_CACHE_DROP_BEHIND_WRITES) == null) ?
        null : conf.getBoolean(DFS_CLIENT_CACHE_DROP_BEHIND_WRITES, false);
    this.defaultReadCachingStrategy =
        new CachingStrategy(readDropBehind, readahead);
    this.defaultWriteCachingStrategy =
        new CachingStrategy(writeDropBehind, readahead);
    this.clientContext = ClientContext.get(
        conf.get(DFS_CLIENT_CONTEXT, DFS_CLIENT_CONTEXT_DEFAULT),
        dfsClientConf);
    /*向多个DN发起读请求，接受最先返回的操作，取消其他操作
    保存了触发“hedgedread”机制的时长。当Client发现一个数据块读取操作太慢时，
    Client会启动另一个并发操作读取数据块的另一个副本，之后Client会返回先完成读取副本的数据。*/
    this.hedgedReadThresholdMillis = conf.getLong(
        DFSConfigKeys.DFS_DFSCLIENT_HEDGED_READ_THRESHOLD_MILLIS,
        DFSConfigKeys.DEFAULT_DFSCLIENT_HEDGED_READ_THRESHOLD_MILLIS);
    int numThreads = conf.getInt(
        DFSConfigKeys.DFS_DFSCLIENT_HEDGED_READ_THREADPOOL_SIZE,
        DFSConfigKeys.DEFAULT_DFSCLIENT_HEDGED_READ_THREADPOOL_SIZE);
    if (numThreads > 0) {
      this.initThreadsNumForHedgedReads(numThreads);
    }
    this.saslClient = new SaslDataTransferClient(
      conf, DataTransferSaslUtil.getSaslPropertiesResolver(conf),
      TrustedChannelResolver.getInstance(conf), nnFallbackToSimpleAuth);
  }
  
  /**
   * Return the socket addresses to use with each configured
   * local interface. Local interfaces may be specified by IP
   * address, IP address range using CIDR notation, interface
   * name (e.g. eth0) or sub-interface name (e.g. eth0:0).
   * The socket addresses consist of the IPs for the interfaces
   * and the ephemeral port (port 0). If an IP, IP range, or
   * interface name matches an interface with sub-interfaces
   * only the IP of the interface is used. Sub-interfaces can
   * be used by specifying them explicitly (by IP or name).
   * 
   * @return SocketAddresses for the configured local interfaces,
   *    or an empty array if none are configured
   * @throws UnknownHostException if a given interface name is invalid
   */
  private static SocketAddress[] getLocalInterfaceAddrs(
      String interfaceNames[]) throws UnknownHostException {
    List<SocketAddress> localAddrs = new ArrayList<SocketAddress>();
    for (String interfaceName : interfaceNames) {
      if (InetAddresses.isInetAddress(interfaceName)) {
        localAddrs.add(new InetSocketAddress(interfaceName, 0));
      } else if (NetUtils.isValidSubnet(interfaceName)) {
        for (InetAddress addr : NetUtils.getIPs(interfaceName, false)) {
          localAddrs.add(new InetSocketAddress(addr, 0));
        }
      } else {
        for (String ip : DNS.getIPs(interfaceName, false)) {
          localAddrs.add(new InetSocketAddress(ip, 0));
        }
      }
    }
    return localAddrs.toArray(new SocketAddress[localAddrs.size()]);
  }

  /**
   * Select one of the configured local interfaces at random. We use a random
   * interface because other policies like round-robin are less effective
   * given that we cache connections to datanodes.
   *
   * @return one of the local interface addresses at random, or null if no
   *    local interfaces are configured
   */
  SocketAddress getRandomLocalInterfaceAddr() {
    if (localInterfaceAddrs.length == 0) {
      return null;
    }
    final int idx = r.nextInt(localInterfaceAddrs.length);
    final SocketAddress addr = localInterfaceAddrs[idx];
    if (LOG.isDebugEnabled()) {
      LOG.debug("Using local interface " + addr);
    }
    return addr;
  }

  /**
   * Return the number of times the client should go back to the namenode
   * to retrieve block locations when reading.
   * 返回客户端返回namenode在读取时检索块位置的次数。
   */
  int getMaxBlockAcquireFailures() {
    return dfsClientConf.maxBlockAcquireFailures;
  }

  /**
   * Return the timeout that clients should use when writing to datanodes.
   * 一个DN超时，会向剩下的DN进行写操作，因此超时时间需要乘以块的个数
   * @param numNodes the number of nodes in the pipeline.
   */
  int getDatanodeWriteTimeout(int numNodes) {
    return (dfsClientConf.confTime > 0) ?
      (dfsClientConf.confTime + HdfsServerConstants.WRITE_TIMEOUT_EXTENSION * numNodes) : 0;
  }

  int getDatanodeReadTimeout(int numNodes) {
    return dfsClientConf.socketTimeout > 0 ?
        (HdfsServerConstants.READ_TIMEOUT_EXTENSION * numNodes +
            dfsClientConf.socketTimeout) : 0;
  }
  
  int getHdfsTimeout() {
    return dfsClientConf.hdfsTimeout;
  }
  
  @VisibleForTesting
  public String getClientName() {
    return clientName;
  }

  //检查文件系统是否打开？
  void checkOpen() throws IOException {
    if (!clientRunning) {
      IOException result = new IOException("Filesystem closed");
      throw result;
    }
  }

  /** Return the lease renewer instance. The renewer thread won't start
   *  until the first output stream is created. The same instance will
   *  be returned until all output streams are closed.
   *  获取重建租约？
   */
  public LeaseRenewer getLeaseRenewer() throws IOException {
      return LeaseRenewer.getInstance(authority, ugi, this);
  }

  /** Get a lease and start automatic renewal */
  private void beginFileLease(final long inodeId, final DFSOutputStream out)
      throws IOException {
    getLeaseRenewer().put(inodeId, out, this);
  }

  /** Stop renewal of lease for the file. */
  void endFileLease(final long inodeId) throws IOException {
    getLeaseRenewer().closeFile(inodeId, this);
  }
    

  /** Put a file. Only called from LeaseRenewer, where proper locking is
   *  enforced to consistently update its local dfsclients array and 
   *  client's filesBeingWritten map.
   *   放一个文件。只从LeaseReNeWER调用，
   *   在那里执行适当的锁定以不断更新其本地DFSClient数组和客户端的文件写入映射。
   */
  void putFileBeingWritten(final long inodeId, final DFSOutputStream out) {
    synchronized(filesBeingWritten) {
      filesBeingWritten.put(inodeId, out);
      // update the last lease renewal time only when there was no
      // writes. once there is one write stream open, the lease renewer
      // thread keeps it updated well with in anyone's expiration time.
    //仅在没有写入时更新上次续租时间。 一旦打开了一个写入流，租约续订线程就会在任何人的到期时间内保持更新。
      if (lastLeaseRenewal == 0) {
        updateLastLeaseRenewal();
      }
    }
  }

  /** Remove a file. Only called from LeaseRenewer. */
  void removeFileBeingWritten(final long inodeId) {
    synchronized(filesBeingWritten) {
      filesBeingWritten.remove(inodeId);
      if (filesBeingWritten.isEmpty()) {
        lastLeaseRenewal = 0;
      }
    }
  }

  /** Is file-being-written map empty? */
  boolean isFilesBeingWrittenEmpty() {
    synchronized(filesBeingWritten) {
      return filesBeingWritten.isEmpty();
    }
  }
  
  /** @return true if the client is running */
  boolean isClientRunning() {
    return clientRunning;
  }

  long getLastLeaseRenewal() {
    return lastLeaseRenewal;
  }

  void updateLastLeaseRenewal() {
    synchronized(filesBeingWritten) {
      if (filesBeingWritten.isEmpty()) {
        return;
      }
      lastLeaseRenewal = Time.monotonicNow();
    }
  }

  /**
   * Renew leases.
   * @return true if lease was renewed. May return false if this
   * client has been closed or has no files open.
   **/
  boolean renewLease() throws IOException {
    if (clientRunning && !isFilesBeingWrittenEmpty()) {
      try {
        namenode.renewLease(clientName);
        updateLastLeaseRenewal();
        return true;
      } catch (IOException e) {
        // Abort if the lease has already expired. 
        final long elapsed = Time.monotonicNow() - getLastLeaseRenewal();
        if (elapsed > HdfsConstants.LEASE_HARDLIMIT_PERIOD) {
          LOG.warn("Failed to renew lease for " + clientName + " for "
              + (elapsed/1000) + " seconds (>= hard-limit ="
              + (HdfsConstants.LEASE_HARDLIMIT_PERIOD/1000) + " seconds.) "
              + "Closing all files being written ...", e);
          closeAllFilesBeingWritten(true);
        } else {
          // Let the lease renewer handle it and retry.
          throw e;
        }
      }
    }
    return false;
  }
  
  /**
   * Close connections the Namenode.
   */
  void closeConnectionToNamenode() {
    RPC.stopProxy(namenode);
  }

  /** Close/abort all files being written. */
  public void closeAllFilesBeingWritten(final boolean abort) {
    for(;;) {
      final long inodeId;
      final DFSOutputStream out;
      synchronized(filesBeingWritten) {
        if (filesBeingWritten.isEmpty()) {
          return;
        }
        inodeId = filesBeingWritten.keySet().iterator().next();
        out = filesBeingWritten.remove(inodeId);
      }
      if (out != null) {
        try {
          if (abort) {
            out.abort();
          } else {
            out.close();
          }
        } catch(IOException ie) {
          LOG.error("Failed to " + (abort? "abort": "close") +
                  " inode " + inodeId, ie);
        }
      }
    }
  }

  /**
   * Close the file system, abandoning all of the leases and files being
   * created and close connections to the namenode.
   */
  @Override
  public synchronized void close() throws IOException {
    if(clientRunning) {
      closeAllFilesBeingWritten(false);
      clientRunning = false;
      getLeaseRenewer().closeClient(this);
      // close connections to the namenode
      closeConnectionToNamenode();
    }
  }

  /**
   * Close all open streams, abandoning all of the leases and files being
   * created.
   * @param abort whether streams should be gracefully closed
   */
  public void closeOutputStreams(boolean abort) {
    if (clientRunning) {
      closeAllFilesBeingWritten(abort);
    }
  }

  /**
   * Get the default block size for this cluster
   * @return the default block size in bytes
   * 得到文件块的默认大小
   */
  public long getDefaultBlockSize() {
    return dfsClientConf.defaultBlockSize;
  }
    
  /**
   * @see ClientProtocol#getPreferredBlockSize(String)
   * 用户设定的块大小？
   */
  public long getBlockSize(String f) throws IOException {
    TraceScope scope = getPathTraceScope("getBlockSize", f);
    try {
      return namenode.getPreferredBlockSize(f);
    } catch (IOException ie) {
      LOG.warn("Problem getting block size", ie);
      throw ie;
    } finally {
      scope.close();
    }
  }

  /**
   * Get server default values for a number of configuration params.
   * @see ClientProtocol#getServerDefaults()
   */
  public FsServerDefaults getServerDefaults() throws IOException {
    long now = Time.monotonicNow();
    if ((serverDefaults == null) ||
        (now - serverDefaultsLastUpdate > SERVER_DEFAULTS_VALIDITY_PERIOD)) {
      serverDefaults = namenode.getServerDefaults();
      serverDefaultsLastUpdate = now;
    }
  //表达式为true，程序继续执行；否则，程序抛出AssertionError，并终止执行。
    assert serverDefaults != null;
    return serverDefaults;
  }
  
  /**
   * Get a canonical（典型的？） token service name for this client's tokens.  Null should
   * be returned if the client is not using tokens.
   * @return the token service for the client
   */
  @InterfaceAudience.LimitedPrivate( { "HDFS" }) 
  public String getCanonicalServiceName() {
    return (dtService != null) ? dtService.toString() : null;
  }
  
  /**
   * @see ClientProtocol#getDelegationToken(Text)---->Delegation（授权）
   */
  public Token<DelegationTokenIdentifier> getDelegationToken(Text renewer)
      throws IOException {
    assert dtService != null;
    TraceScope scope = Trace.startSpan("getDelegationToken", traceSampler);
    try {//创建新的DelegationToken
      Token<DelegationTokenIdentifier> token =
        namenode.getDelegationToken(renewer);
      if (token != null) {
        token.setService(this.dtService);
        LOG.info("Created " + DelegationTokenIdentifier.stringifyToken(token));
      } else {
        LOG.info("Cannot get delegation token from " + renewer);
      }
      return token;
    } finally {
      scope.close();
    }
  }

  /**
   * Renew a delegation token
   * @param token the token to renew
   * @return the new expiration time
   * @throws InvalidToken
   * @throws IOException
   * @deprecated Use Token.renew instead.
   */
  @Deprecated
  public long renewDelegationToken(Token<DelegationTokenIdentifier> token)
      throws InvalidToken, IOException {
    LOG.info("Renewing " + DelegationTokenIdentifier.stringifyToken(token));
    try {
      return token.renew(conf);
    } catch (InterruptedException ie) {                                       
      throw new RuntimeException("caught interrupted", ie);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(InvalidToken.class,
                                     AccessControlException.class);
    }
  }
  
  private static final Map<String, Boolean> localAddrMap = Collections
      .synchronizedMap(new HashMap<String, Boolean>());
  
  public static boolean isLocalAddress(InetSocketAddress targetAddr) {
    InetAddress addr = targetAddr.getAddress();
    Boolean cached = localAddrMap.get(addr.getHostAddress());
    if (cached != null) {
      if (LOG.isTraceEnabled()) {
        LOG.trace("Address " + targetAddr +
                  (cached ? " is local" : " is not local"));
      }
      return cached;
    }
    
    boolean local = NetUtils.isLocalAddress(addr);

    if (LOG.isTraceEnabled()) {
      LOG.trace("Address " + targetAddr +
                (local ? " is local" : " is not local"));
    }
    localAddrMap.put(addr.getHostAddress(), local);
    return local;
  }
  
  /**
   * Cancel a delegation token
   * @param token the token to cancel
   * @throws InvalidToken
   * @throws IOException
   * @deprecated Use Token.cancel instead.
   */
  @Deprecated
  public void cancelDelegationToken(Token<DelegationTokenIdentifier> token)
      throws InvalidToken, IOException {
    LOG.info("Cancelling " + DelegationTokenIdentifier.stringifyToken(token));
    try {
      token.cancel(conf);
     } catch (InterruptedException ie) {                                       
      throw new RuntimeException("caught interrupted", ie);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(InvalidToken.class,
                                     AccessControlException.class);
    }
  }
  
  @InterfaceAudience.Private
  public static class Renewer extends TokenRenewer {
    
    static {
      //Ensure that HDFS Configuration files are loaded before trying to use
      // the renewer.
      HdfsConfiguration.init();
    }
    
    @Override
    public boolean handleKind(Text kind) {
      return DelegationTokenIdentifier.HDFS_DELEGATION_KIND.equals(kind);
    }

    @SuppressWarnings("unchecked")
    @Override
    public long renew(Token<?> token, Configuration conf) throws IOException {
      Token<DelegationTokenIdentifier> delToken = 
        (Token<DelegationTokenIdentifier>) token;
      ClientProtocol nn = getNNProxy(delToken, conf);
      try {
        return nn.renewDelegationToken(delToken);
      } catch (RemoteException re) {
        throw re.unwrapRemoteException(InvalidToken.class, 
                                       AccessControlException.class);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void cancel(Token<?> token, Configuration conf) throws IOException {
      Token<DelegationTokenIdentifier> delToken = 
          (Token<DelegationTokenIdentifier>) token;
      LOG.info("Cancelling " + 
               DelegationTokenIdentifier.stringifyToken(delToken));
      ClientProtocol nn = getNNProxy(delToken, conf);
      try {
        nn.cancelDelegationToken(delToken);
      } catch (RemoteException re) {
        throw re.unwrapRemoteException(InvalidToken.class,
            AccessControlException.class);
      }
    }
    
    private static ClientProtocol getNNProxy(
        Token<DelegationTokenIdentifier> token, Configuration conf)
        throws IOException {
      URI uri = HAUtil.getServiceUriFromToken(HdfsConstants.HDFS_URI_SCHEME,
              token);
      if (HAUtil.isTokenForLogicalUri(token) &&
          !HAUtil.isLogicalUri(conf, uri)) {
        // If the token is for a logical nameservice, but the configuration
        // we have disagrees about that, we can't actually renew it.
        // This can be the case in MR, for example, if the RM doesn't
        // have all of the HA clusters configured in its configuration.
        throw new IOException("Unable to map logical nameservice URI '" +
            uri + "' to a NameNode. Local configuration does not have " +
            "a failover proxy provider configured.");
      }
      
      NameNodeProxies.ProxyAndInfo<ClientProtocol> info =
        NameNodeProxies.createProxy(conf, uri, ClientProtocol.class);
      assert info.getDelegationTokenService().equals(token.getService()) :
        "Returned service '" + info.getDelegationTokenService().toString() +
        "' doesn't match expected service '" +
        token.getService().toString() + "'";
        
      return info.getProxy();
    }

    @Override
    public boolean isManaged(Token<?> token) throws IOException {
      return true;
    }
    
  }

  /**
   * Report corrupt blocks that were discovered by the client.
   * @see ClientProtocol#reportBadBlocks(LocatedBlock[])
   */
  public void reportBadBlocks(LocatedBlock[] blocks) throws IOException {
    namenode.reportBadBlocks(blocks);
  }
  
//得到文件块的默认副本数
  public short getDefaultReplication() {
    return dfsClientConf.defaultReplication;
  }
  
  public LocatedBlocks getLocatedBlocks(String src, long start)
      throws IOException {
    return getLocatedBlocks(src, start, dfsClientConf.prefetchSize);
  }

  /*
   * This is just a wrapper around callGetBlockLocations, but non-static so that
   * we can stub it out for tests.
   */
  @VisibleForTesting
  public LocatedBlocks getLocatedBlocks(String src, long start, long length)
      throws IOException {
    TraceScope scope = getPathTraceScope("getBlockLocations", src);
    try {
      return callGetBlockLocations(namenode, src, start, length);
    } finally {
      scope.close();
    }
  }

  /**
   * @see ClientProtocol#getBlockLocations(String, long, long)
   */
  static LocatedBlocks callGetBlockLocations(ClientProtocol namenode,
      String src, long start, long length) 
      throws IOException {
    try {
      return namenode.getBlockLocations(src, start, length);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    }
  }

  /**
   * Recover a file's lease
   * @param src a file's path
   * @return true if the file is already closed
   * @throws IOException
   * 恢复一个文件的租约，文件被关闭后，返回true
   */
  boolean recoverLease(String src) throws IOException {
    checkOpen();

    TraceScope scope = getPathTraceScope("recoverLease", src);
    try {
      return namenode.recoverLease(src, clientName);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(FileNotFoundException.class,
                                     AccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Get block location info about file
   * 获取文件的数据块的位置信息，返回的是主机名的List
   * getBlockLocations() returns a list of hostnames that store 
   * data for a specific file region.  It returns a set of hostnames
   * for every block within the indicated region.
   *
   * This function is very useful when writing code that considers
   * data-placement when performing operations.  For example, the
   * MapReduce system tries to schedule tasks on the same machines
   * as the data-block the task processes. 
   */
  public BlockLocation[] getBlockLocations(String src, long start, 
        long length) throws IOException, UnresolvedLinkException {
    TraceScope scope = getPathTraceScope("getBlockLocations", src);
    try {
      //获取文件分配的数据块
      LocatedBlocks blocks = getLocatedBlocks(src, start, length);
      BlockLocation[] locations =  DFSUtil.locatedBlocks2Locations(blocks);
      HdfsBlockLocation[] hdfsLocations = new HdfsBlockLocation[locations.length];
      //获取对应数据块的位置？
      for (int i = 0; i < locations.length; i++) {
        hdfsLocations[i] = new HdfsBlockLocation(locations[i], blocks.get(i));
      }
      return hdfsLocations;
    } finally {
      scope.close();
    }
  }
  
  /**
   * Get block location information about a list of {@link HdfsBlockLocation}.
   * Used by {@link DistributedFileSystem#getFileBlockStorageLocations(List)} to
   * get {@link BlockStorageLocation}s for blocks returned by
   * {@link DistributedFileSystem#getFileBlockLocations(org.apache.hadoop.fs.FileStatus, long, long)}
   * .
   * 
   * This is done by making a round of RPCs to the associated datanodes, asking
   * the volume of each block replica. The returned array of
   * {@link BlockStorageLocation} expose this information as a
   * {@link VolumeId}.
   * 
   * @param blockLocations
   *          target blocks on which to query volume location information
   * @return volumeBlockLocations original block array augmented with additional
   *         volume location information for each replica.
   */
  public BlockStorageLocation[] getBlockStorageLocations(
      List<BlockLocation> blockLocations) throws IOException,
      UnsupportedOperationException, InvalidBlockTokenException {
    if (!getConf().getHdfsBlocksMetadataEnabled) {//需要client conf中设置是否允许读取元数据
      throw new UnsupportedOperationException("Datanode-side support for " +
          "getVolumeBlockLocations() must also be enabled in the client " +
          "configuration.");
    }
    // Downcast blockLocations and fetch out required LocatedBlock(s)
    List<LocatedBlock> blocks = new ArrayList<LocatedBlock>();
    for (BlockLocation loc : blockLocations) {
      if (!(loc instanceof HdfsBlockLocation)) {
        throw new ClassCastException("DFSClient#getVolumeBlockLocations " +
            "expected to be passed HdfsBlockLocations");
      }
      HdfsBlockLocation hdfsLoc = (HdfsBlockLocation) loc;
      blocks.add(hdfsLoc.getLocatedBlock());
    }
    
    // Re-group the LocatedBlocks to be grouped by datanodes, with the values
    // a list of the LocatedBlocks on the datanode.
    Map<DatanodeInfo, List<LocatedBlock>> datanodeBlocks = 
        new LinkedHashMap<DatanodeInfo, List<LocatedBlock>>();
    for (LocatedBlock b : blocks) {
      for (DatanodeInfo info : b.getLocations()) {
        if (!datanodeBlocks.containsKey(info)) {
          datanodeBlocks.put(info, new ArrayList<LocatedBlock>());
        }
        List<LocatedBlock> l = datanodeBlocks.get(info);
        l.add(b);
      }
    }
        
    // Make RPCs to the datanodes to get volume locations for its replicas
    TraceScope scope =
      Trace.startSpan("getBlockStorageLocations", traceSampler);
    Map<DatanodeInfo, HdfsBlocksMetadata> metadatas;
    try {
      metadatas = BlockStorageLocationUtil.
          queryDatanodesForHdfsBlocksMetadata(conf, datanodeBlocks,
              getConf().getFileBlockStorageLocationsNumThreads,
              getConf().getFileBlockStorageLocationsTimeoutMs,
              getConf().connectToDnViaHostname);
      if (LOG.isTraceEnabled()) {
        LOG.trace("metadata returned: "
            + Joiner.on("\n").withKeyValueSeparator("=").join(metadatas));
      }
    } finally {
      scope.close();
    }
    
    // Regroup the returned VolumeId metadata to again be grouped by
    // LocatedBlock rather than by datanode
    Map<LocatedBlock, List<VolumeId>> blockVolumeIds = BlockStorageLocationUtil
        .associateVolumeIdsWithBlocks(blocks, metadatas);
    
    // Combine original BlockLocations with new VolumeId information
    BlockStorageLocation[] volumeBlockLocations = BlockStorageLocationUtil
        .convertToVolumeBlockLocations(blocks, blockVolumeIds);

    return volumeBlockLocations;
  }

  /**
   * Decrypts a EDEK by consulting the KeyProvider.
   * 解密加密数据的加密秘钥
   */
  private KeyVersion decryptEncryptedDataEncryptionKey(FileEncryptionInfo
      feInfo) throws IOException {
    TraceScope scope = Trace.startSpan("decryptEDEK", traceSampler);
    try {
      KeyProvider provider = getKeyProvider();
      if (provider == null) {
        throw new IOException("No KeyProvider is configured, cannot access" +
            " an encrypted file");
      }
      EncryptedKeyVersion ekv = EncryptedKeyVersion.createForDecryption(
          feInfo.getKeyName(), feInfo.getEzKeyVersionName(), feInfo.getIV(),
          feInfo.getEncryptedDataEncryptionKey());
      try {
        KeyProviderCryptoExtension cryptoProvider = KeyProviderCryptoExtension
            .createKeyProviderCryptoExtension(provider);
        return cryptoProvider.decryptEncryptedKey(ekv);
      } catch (GeneralSecurityException e) {
        throw new IOException(e);
      }
    } finally {
      scope.close();
    }
  }

  /**
   * Obtain the crypto protocol version from the provided FileEncryptionInfo,
   * checking to see if this version is supported by.
   * 返回加密版本，并且检查是否支持该版本？
   * @param feInfo FileEncryptionInfo
   * @return CryptoProtocolVersion from the feInfo
   * @throws IOException if the protocol version is unsupported.
   */
  private static CryptoProtocolVersion getCryptoProtocolVersion
      (FileEncryptionInfo feInfo) throws IOException {
    final CryptoProtocolVersion version = feInfo.getCryptoProtocolVersion();
    if (!CryptoProtocolVersion.supports(version)) {
      throw new IOException("Client does not support specified " +
          "CryptoProtocolVersion " + version.getDescription() + " version " +
          "number" + version.getVersion());
    }
    return version;
  }

  /**
   * Obtain a CryptoCodec based on the CipherSuite（加密套件） set in a FileEncryptionInfo
   * and the available CryptoCodecs configured in the Configuration.
   *
   * @param conf   Configuration
   * @param feInfo FileEncryptionInfo
   * @return CryptoCodec
   * @throws IOException if no suitable CryptoCodec for the CipherSuite is
   *                     available.
   */
  private static CryptoCodec getCryptoCodec(Configuration conf,
      FileEncryptionInfo feInfo) throws IOException {
    final CipherSuite suite = feInfo.getCipherSuite();
    //先检查suite是否为unknown
    if (suite.equals(CipherSuite.UNKNOWN)) {
      throw new IOException("NameNode specified unknown CipherSuite with ID "
          + suite.getUnknownValue() + ", cannot instantiate CryptoCodec.");
    }
    final CryptoCodec codec = CryptoCodec.getInstance(conf, suite);
    if (codec == null) {
      throw new UnknownCipherSuiteException(
          "No configuration found for the cipher suite "
          + suite.getConfigSuffix() + " prefixed with "
          + HADOOP_SECURITY_CRYPTO_CODEC_CLASSES_KEY_PREFIX
          + ". Please see the example configuration "
          + "hadoop.security.crypto.codec.classes.EXAMPLECIPHERSUITE "
          + "at core-default.xml for details.");
    }
    return codec;
  }

  /**
   * Wraps the stream in a CryptoInputStream if the underlying file is
   * encrypted.
   */
  public HdfsDataInputStream createWrappedInputStream(DFSInputStream dfsis)
      throws IOException {
    final FileEncryptionInfo feInfo = dfsis.getFileEncryptionInfo();
    if (feInfo != null) {
      // File is encrypted, wrap the stream in a crypto stream.
      // Currently only one version, so no special logic based on the version #
      getCryptoProtocolVersion(feInfo);
      final CryptoCodec codec = getCryptoCodec(conf, feInfo);
      final KeyVersion decrypted = decryptEncryptedDataEncryptionKey(feInfo);
      final CryptoInputStream cryptoIn =
          new CryptoInputStream(dfsis, codec, decrypted.getMaterial(),
              feInfo.getIV());//inputstream对应的read，解密
      return new HdfsDataInputStream(cryptoIn);
    } else {
      // No FileEncryptionInfo so no encryption.
      return new HdfsDataInputStream(dfsis);
    }
  }

  /**
   * Wraps the stream in a CryptoOutputStream if the underlying file is
   * encrypted.
   */
  public HdfsDataOutputStream createWrappedOutputStream(DFSOutputStream dfsos,
      FileSystem.Statistics statistics) throws IOException {
    return createWrappedOutputStream(dfsos, statistics, 0);
  }

  /**
   * Wraps the stream in a CryptoOutputStream if the underlying file is
   * encrypted.
   */
  public HdfsDataOutputStream createWrappedOutputStream(DFSOutputStream dfsos,
      FileSystem.Statistics statistics, long startPos) throws IOException {
    final FileEncryptionInfo feInfo = dfsos.getFileEncryptionInfo();
    if (feInfo != null) {
      // File is encrypted, wrap the stream in a crypto stream.
      // Currently only one version, so no special logic based on the version #
      getCryptoProtocolVersion(feInfo);
      final CryptoCodec codec = getCryptoCodec(conf, feInfo);
      KeyVersion decrypted = decryptEncryptedDataEncryptionKey(feInfo);
      final CryptoOutputStream cryptoOut =
          new CryptoOutputStream(dfsos, codec,
              decrypted.getMaterial(), feInfo.getIV(), startPos);
      return new HdfsDataOutputStream(cryptoOut, statistics, startPos);
    } else {
      // No FileEncryptionInfo present so no encryption.
      return new HdfsDataOutputStream(dfsos, statistics, startPos);
    }
  }

  public DFSInputStream open(String src) 
      throws IOException, UnresolvedLinkException {
    return open(src, dfsClientConf.ioBufferSize, true, null);
  }

  /**
   * Create an input stream that obtains a nodelist from the
   * namenode, and then reads from all the right places.  Creates
   * inner subclass of InputStream that does the right out-of-band
   * work
   * @deprecated Use {@link #open(String, int, boolean)} instead.
   */
  @Deprecated
  public DFSInputStream open(String src, int buffersize, boolean verifyChecksum,
                             FileSystem.Statistics stats)
      throws IOException, UnresolvedLinkException {
    return open(src, buffersize, verifyChecksum);
  }
  

  /**
   * Create an input stream that obtains a nodelist from the
   * namenode, and then reads from all the right places.  Creates
   * inner subclass of InputStream that does the right out-of-band
   * work.打开指定文件，用于返回读取文件内容的输入流DFSInputStream
   */
  public DFSInputStream open(String src, int buffersize, boolean verifyChecksum)
      throws IOException, UnresolvedLinkException {
    checkOpen();
    //    Get block info from namenode
    TraceScope scope = getPathTraceScope("newDFSInputStream", src);
    try {
      return new DFSInputStream(this, src, verifyChecksum);
    } finally {
      scope.close();
    }
  }

  /**
   * Get the namenode associated with this DFSClient object
   * @return the namenode associated with this DFSClient object
   */
  public ClientProtocol getNamenode() {
    return namenode;
  }
  
  /**
   * Call {@link #create(String, boolean, short, long, Progressable)} with
   * default <code>replication</code> and <code>blockSize<code> and null <code>
   * progress</code>.
   */
  public OutputStream create(String src, boolean overwrite) 
      throws IOException {
    return create(src, overwrite, dfsClientConf.defaultReplication,
        dfsClientConf.defaultBlockSize, null);
  }
    
  /**
   * Call {@link #create(String, boolean, short, long, Progressable)} with
   * default <code>replication</code> and <code>blockSize<code>.
   */
  public OutputStream create(String src, 
                             boolean overwrite,
                             Progressable progress) throws IOException {
    return create(src, overwrite, dfsClientConf.defaultReplication,
        dfsClientConf.defaultBlockSize, progress);
  }
    
  /**
   * Call {@link #create(String, boolean, short, long, Progressable)} with
   * null <code>progress</code>.
   */
  public OutputStream create(String src, 
                             boolean overwrite, 
                             short replication,
                             long blockSize) throws IOException {
    return create(src, overwrite, replication, blockSize, null);
  }

  /**
   * Call {@link #create(String, boolean, short, long, Progressable, int)}
   * with default bufferSize.
   */
  public OutputStream create(String src, boolean overwrite, short replication,
      long blockSize, Progressable progress) throws IOException {
    return create(src, overwrite, replication, blockSize, progress,
        dfsClientConf.ioBufferSize);
  }

  /**
   * Call {@link #create(String, FsPermission, EnumSet, short, long, 
   * Progressable, int, ChecksumOpt)} with default <code>permission</code>
   * {@link FsPermission#getFileDefault()}.
   * 
   * @param src File name
   * @param overwrite overwrite an existing file if true
   * @param replication replication factor for the file
   * @param blockSize maximum block size
   * @param progress interface for reporting client progress
   * @param buffersize underlying buffersize
   * 
   * @return output stream
   */
  public OutputStream create(String src,
                             boolean overwrite,
                             short replication,
                             long blockSize,
                             Progressable progress,
                             int buffersize)
      throws IOException {
    return create(src, FsPermission.getFileDefault(),
        overwrite ? EnumSet.of(CreateFlag.CREATE, CreateFlag.OVERWRITE)
            : EnumSet.of(CreateFlag.CREATE), replication, blockSize, progress,
        buffersize, null);
  }

  /**
   * Call {@link #create(String, FsPermission, EnumSet, boolean, short, 
   * long, Progressable, int, ChecksumOpt)} with <code>createParent</code>
   *  set to true.
   */
  public DFSOutputStream create(String src, 
                             FsPermission permission,
                             EnumSet<CreateFlag> flag, 
                             short replication,
                             long blockSize,
                             Progressable progress,
                             int buffersize,
                             ChecksumOpt checksumOpt)
      throws IOException {
    return create(src, permission, flag, true,
        replication, blockSize, progress, buffersize, checksumOpt, null);
  }

  /**
   * Create a new dfs file with the specified block replication 
   * with write-progress reporting and return an output stream for writing
   * into the file.  
   * 
   * @param src File name
   * @param permission The permission of the directory being created.
   *          If null, use default permission {@link FsPermission#getFileDefault()}
   * @param flag indicates create a new file or create/overwrite an
   *          existing file or append to an existing file
   * @param createParent create missing parent directory if true
   * @param replication block replication
   * @param blockSize maximum block size
   * @param progress interface for reporting client progress
   * @param buffersize underlying buffer size 
   * @param checksumOpt checksum options
   * 
   * @return output stream
   *
   * @see ClientProtocol#create for detailed description of exceptions thrown
   */
  public DFSOutputStream create(String src, 
                             FsPermission permission,
                             EnumSet<CreateFlag> flag, 
                             boolean createParent,
                             short replication,
                             long blockSize,
                             Progressable progress,
                             int buffersize,
                             ChecksumOpt checksumOpt) throws IOException {
    return create(src, permission, flag, createParent, replication, blockSize, 
        progress, buffersize, checksumOpt, null);
  }

  /**
   * Same as {@link #create(String, FsPermission, EnumSet, boolean, short, long,
   * Progressable, int, ChecksumOpt)} with the addition of favoredNodes that is
   * a hint to where the namenode should place the file blocks.
   * The favored nodes hint is not persisted in HDFS. Hence it may be honored
   * at the creation time only. HDFS could move the blocks during balancing or
   * replication, to move the blocks from favored nodes. A value of null means
   * no favored nodes for this create
   * HDFS中不保留优选节点提示。 因此，它可能仅在创建时受到尊重。 
   * HDFS可以在平衡或复制期间移动块，以从有利节点移动块。 值null表示此创建没有优先节点
   */
  public DFSOutputStream create(String src, 
                             FsPermission permission,
                             EnumSet<CreateFlag> flag, 
                             boolean createParent,
                             short replication,
                             long blockSize,
                             Progressable progress,
                             int buffersize,
                             ChecksumOpt checksumOpt,
                             InetSocketAddress[] favoredNodes) throws IOException {
    checkOpen();
    if (permission == null) {
      permission = FsPermission.getFileDefault();
    }
    FsPermission masked = permission.applyUMask(dfsClientConf.uMask);
    if(LOG.isDebugEnabled()) {
      LOG.debug(src + ": masked=" + masked);
    }
    final DFSOutputStream result = DFSOutputStream.newStreamForCreate(this,
        src, masked, flag, createParent, replication, blockSize, progress,
        buffersize, dfsClientConf.createChecksum(checksumOpt),
        getFavoredNodesStr(favoredNodes));
    beginFileLease(result.getFileId(), result);
    return result;
  }

 //以String数组的形式，获取优选节点的host信息
  private String[] getFavoredNodesStr(InetSocketAddress[] favoredNodes) {
    String[] favoredNodeStrs = null;
    if (favoredNodes != null) {
      favoredNodeStrs = new String[favoredNodes.length];
      for (int i = 0; i < favoredNodes.length; i++) {
        favoredNodeStrs[i] = 
            favoredNodes[i].getHostName() + ":" 
                         + favoredNodes[i].getPort();
      }
    }
    return favoredNodeStrs;
  }
  
  /**
   * Append to an existing file if {@link CreateFlag#APPEND} is present
   */
  private DFSOutputStream primitiveAppend(String src, EnumSet<CreateFlag> flag,
      int buffersize, Progressable progress) throws IOException {
    if (flag.contains(CreateFlag.APPEND)) {//文件是否允许追加写
      HdfsFileStatus stat = getFileInfo(src);
      if (stat == null) { // No file to append to
        // New file needs to be created if create option is present
        if (!flag.contains(CreateFlag.CREATE)) {//文件是否允许创建
          throw new FileNotFoundException("failed to append to non-existent file "
              + src + " on client " + clientName);
        }
        return null;
      }
      return callAppend(src, buffersize, flag, progress, null);
    }
    return null;
  }
  
  /**
   * Same as {{@link #create(String, FsPermission, EnumSet, short, long,
   *  Progressable, int, ChecksumOpt)} except that the permission
   *  is absolute (ie has already been masked with umask.
   */
  public DFSOutputStream primitiveCreate(String src, 
                             FsPermission absPermission,
                             EnumSet<CreateFlag> flag,
                             boolean createParent,
                             short replication,
                             long blockSize,
                             Progressable progress,
                             int buffersize,
                             ChecksumOpt checksumOpt)
      throws IOException, UnresolvedLinkException {
    checkOpen();
    CreateFlag.validate(flag);
    DFSOutputStream result = primitiveAppend(src, flag, buffersize, progress);
    if (result == null) {
      DataChecksum checksum = dfsClientConf.createChecksum(checksumOpt);
      result = DFSOutputStream.newStreamForCreate(this, src, absPermission,
          flag, createParent, replication, blockSize, progress, buffersize,
          checksum, null);
    }
    beginFileLease(result.getFileId(), result);
    return result;
  }
  
  /**
   * Creates a symbolic link.
   * 
   * @see ClientProtocol#createSymlink(String, String,FsPermission, boolean) 
   */
  public void createSymlink(String target, String link, boolean createParent)
      throws IOException {
    TraceScope scope = getPathTraceScope("createSymlink", target);
    try {
      FsPermission dirPerm = 
          FsPermission.getDefault().applyUMask(dfsClientConf.uMask); 
      namenode.createSymlink(target, link, dirPerm, createParent);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileAlreadyExistsException.class, 
                                     FileNotFoundException.class,
                                     ParentNotDirectoryException.class,
                                     NSQuotaExceededException.class, 
                                     DSQuotaExceededException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {//不管执行是否成功，都要close
      scope.close();
    }
  }

  /**
   * Resolve the *first* symlink, if any, in the path.
   * 
   * @see ClientProtocol#getLinkTarget(String)
   */
  public String getLinkTarget(String path) throws IOException { 
    checkOpen();
    TraceScope scope = getPathTraceScope("getLinkTarget", path);
    try {
      return namenode.getLinkTarget(path);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class);
    } finally {
      scope.close();
    }
  }

  /** Method to get stream returned by append call */
  private DFSOutputStream callAppend(String src, int buffersize,
      EnumSet<CreateFlag> flag, Progressable progress, String[] favoredNodes)
      throws IOException {
    CreateFlag.validateForAppend(flag);//检查是否允许追加写？
    try {
      LastBlockWithStatus blkWithStatus = namenode.append(src, clientName,
          new EnumSetWritable<>(flag, CreateFlag.class));
      HdfsFileStatus status = blkWithStatus.getFileStatus();
      if (status == null) {
        DFSClient.LOG.debug("NameNode is on an older version, request file " +
            "info with additional RPC call for file: " + src);
        status = getFileInfo(src);//更新status
      }
      return DFSOutputStream.newStreamForAppend(this, src, flag, buffersize,
          progress, blkWithStatus.getLastBlock(),
          status, dfsClientConf.createChecksum(),
          favoredNodes);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     SafeModeException.class,
                                     DSQuotaExceededException.class,
                                     UnsupportedOperationException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    }
  }
  
  /**
   * Append to an existing HDFS file.  
   * 写入数据使用append函数，返回outputstream
   * @param src file name
   * @param buffersize buffer size
   * @param flag indicates whether to append data to a new block instead of
   *             the last block
   * @param progress for reporting write-progress; null is acceptable.
   * @param statistics file system statistics; null is acceptable.
   * @return an output stream for writing into the file
   * 
   * @see ClientProtocol#append(String, String, EnumSetWritable)
   */
  public HdfsDataOutputStream append(final String src, final int buffersize,
      EnumSet<CreateFlag> flag, final Progressable progress,
      final FileSystem.Statistics statistics) throws IOException {
    final DFSOutputStream out = append(src, buffersize, flag, null, progress);
    return createWrappedOutputStream(out, statistics, out.getInitialLen());
  }

  /**
   * Append to an existing HDFS file.
   * 
   * @param src file name
   * @param buffersize buffer size
   * @param flag indicates whether to append data to a new block instead of the
   *          last block
   * @param progress for reporting write-progress; null is acceptable.
   * @param statistics file system statistics; null is acceptable.
   * @param favoredNodes FavoredNodes for new blocks
   * @return an output stream for writing into the file
   * @see ClientProtocol#append(String, String, EnumSetWritable)
   */
  public HdfsDataOutputStream append(final String src, final int buffersize,
      EnumSet<CreateFlag> flag, final Progressable progress,
      final FileSystem.Statistics statistics,
      final InetSocketAddress[] favoredNodes) throws IOException {
    final DFSOutputStream out = append(src, buffersize, flag,
        getFavoredNodesStr(favoredNodes), progress);
    return createWrappedOutputStream(out, statistics, out.getInitialLen());
  }

  private DFSOutputStream append(String src, int buffersize,
      EnumSet<CreateFlag> flag, String[] favoredNodes, Progressable progress)
      throws IOException {
    checkOpen();
    final DFSOutputStream result = callAppend(src, buffersize, flag, progress,
        favoredNodes);
    beginFileLease(result.getFileId(), result);
    return result;
  }

  /**
   * Set replication for an existing file.
   * @param src file name
   * @param replication replication to set the file to
   * 设置已经存在的文件的副本数
   * @see ClientProtocol#setReplication(String, short)
   */
  public boolean setReplication(String src, short replication)
      throws IOException {
    TraceScope scope = getPathTraceScope("setReplication", src);
    try {
      return namenode.setReplication(src, replication);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     SafeModeException.class,
                                     DSQuotaExceededException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Set storage policy for an existing file/directory(设置存储策略)
   * @param src file/directory name
   * @param policyName name of the storage policy
   */
  public void setStoragePolicy(String src, String policyName)
      throws IOException {
    TraceScope scope = getPathTraceScope("setStoragePolicy", src);
    try {
      namenode.setStoragePolicy(src, policyName);
    } catch (RemoteException e) {
      throw e.unwrapRemoteException(AccessControlException.class,
                                    FileNotFoundException.class,
                                    SafeModeException.class,
                                    NSQuotaExceededException.class,
                                    UnresolvedPathException.class,
                                    SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * @return All the existing storage policies（获取所有的存储策略）
   */
  public BlockStoragePolicy[] getStoragePolicies() throws IOException {
    TraceScope scope = Trace.startSpan("getStoragePolicies", traceSampler);
    try {
      return namenode.getStoragePolicies();
    } finally {
      scope.close();
    }
  }

  /**
   * Rename file or directory.（重命名）
   * @see ClientProtocol#rename(String, String)
   * @deprecated Use {@link #rename(String, String, Options.Rename...)} instead.
   */
  @Deprecated
  public boolean rename(String src, String dst) throws IOException {
    checkOpen();
    TraceScope scope = getSrcDstTraceScope("rename", src, dst);
    try {
      return namenode.rename(src, dst);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     NSQuotaExceededException.class,
                                     DSQuotaExceededException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Move blocks from src to trg and delete src(复制，并删除源文件)
   * See {@link ClientProtocol#concat}.
   */
  public void concat(String trg, String [] srcs) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("concat", traceSampler);
    try {
      namenode.concat(trg, srcs);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }
  /**
   * Rename file or directory.
   * @see ClientProtocol#rename2(String, String, Options.Rename...)
   */
  public void rename(String src, String dst, Options.Rename... options)
      throws IOException {
    checkOpen();
    TraceScope scope = getSrcDstTraceScope("rename2", src, dst);
    try {
      namenode.rename2(src, dst, options);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     DSQuotaExceededException.class,
                                     FileAlreadyExistsException.class,
                                     FileNotFoundException.class,
                                     ParentNotDirectoryException.class,
                                     SafeModeException.class,
                                     NSQuotaExceededException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Truncate a file to an indicated size
   * 截短以后对文件的内容并没有删减，只是文件的len属性变为newLength
   * 将文件截断为指定的大小
   * See {@link ClientProtocol#truncate}.
   */
  public boolean truncate(String src, long newLength) throws IOException {
    checkOpen();
    if (newLength < 0) {
      throw new HadoopIllegalArgumentException(
          "Cannot truncate to a negative file size: " + newLength + ".");
    }
    try {
      return namenode.truncate(src, newLength, clientName);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
          UnresolvedPathException.class);
    }
  }

  /**
   * Delete file or directory.（删除文件）
   * See {@link ClientProtocol#delete(String, boolean)}. 
   */
  @Deprecated
  public boolean delete(String src) throws IOException {
    checkOpen();
    return delete(src, true);
  }

  /**
   * delete file or directory.（删除非空的文件）
   * delete contents of the directory if non empty and recursive （递归删除？）
   * set to true （recursive为true，循环删除一个目录下的子目录）
   *
   * @see ClientProtocol#delete(String, boolean)
   */
  public boolean delete(String src, boolean recursive) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("delete", src);
    try {
      return namenode.delete(src, recursive);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     SafeModeException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }
  
  /** Implemented using getFileInfo(src)
   */
  public boolean exists(String src) throws IOException {
    checkOpen();
    return getFileInfo(src) != null;
  }

  /**
   * Get a partial listing of the indicated directory（获取指定目录的部分列表）
   * No block locations need to be fetched
   */
  public DirectoryListing listPaths(String src,  byte[] startAfter)
    throws IOException {
    return listPaths(src, startAfter, false);
  }
  
  /**
   * Get a partial listing of the indicated directory
   *
   * Recommend to use HdfsFileStatus.EMPTY_NAME as startAfter
   * if the application wants to fetch a listing starting from
   * the first entry in the directory
   *
   * @see ClientProtocol#getListing(String, byte[], boolean)
   */
  public DirectoryListing listPaths(String src,  byte[] startAfter,
      boolean needLocation) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("listPaths", src);
    try {
      return namenode.getListing(src, startAfter, needLocation);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }


  /**
   * Get the file info for a specific file or directory.(获取指定文件的信息)
   * @param src The string representation of the path to the file
   * @return object containing information regarding the file
   *         or null if file not found
   *         
   * @see ClientProtocol#getFileInfo(String) for description of exceptions
   */
  public HdfsFileStatus getFileInfo(String src) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("getFileInfo", src);
    try {
      return namenode.getFileInfo(src);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }
  
  /**
   * Close status of a file（文件是否已关闭）
   * @return true if file is already closed
   */
  public boolean isFileClosed(String src) throws IOException{
    checkOpen();
    TraceScope scope = getPathTraceScope("isFileClosed", src);
    try {
      return namenode.isFileClosed(src);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }
  
  /**
   * Get the file info for a specific file or directory. If src
   * refers to a symlink then the FileStatus of the link is returned.
   * @param src path to a file or directory.
   * 
   * For description of exceptions thrown 
   * @see ClientProtocol#getFileLinkInfo(String)
   */
  public HdfsFileStatus getFileLinkInfo(String src) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("getFileLinkInfo", src);
    try {
      return namenode.getFileLinkInfo(src);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
   }
  
  @InterfaceAudience.Private
  public void clearDataEncryptionKey() {
    LOG.debug("Clearing encryption key");
    synchronized (this) {
      encryptionKey = null;
    }
  }
  
  /**
   * @return true if data sent between this client and DNs should be encrypted,
   *         false otherwise.（检查client和datanode之间的数据传输是否需要加密）
   * @throws IOException in the event of error communicating with the NN
   */
  boolean shouldEncryptData() throws IOException {
    FsServerDefaults d = getServerDefaults();
    return d == null ? false : d.getEncryptDataTransfer();
  }
  
  @Override
  public DataEncryptionKey newDataEncryptionKey() throws IOException {
    if (shouldEncryptData()) {//需要加密，如果没有秘钥，就产生一个新的；否则返回原来的
      synchronized (this) {
        if (encryptionKey == null ||
            encryptionKey.expiryDate < Time.now()) {
          LOG.debug("Getting new encryption token from NN");
          encryptionKey = namenode.getDataEncryptionKey();
        }
        return encryptionKey;
      }
    } else {
      return null;
    }
  }

  /**
   * Get the checksum of the whole file of a range of the file. Note that the
   * range always starts from the beginning of the file.
   * @param src The file path
   * @param length the length of the range, i.e., the range is [0, length]
   * @return The checksum 
   * @see DistributedFileSystem#getFileChecksum(Path)
   */
  public MD5MD5CRC32FileChecksum getFileChecksum(String src, long length)
      throws IOException {
    checkOpen();//对文件进行操作，几乎都需要先检查文件是否已经打开
    Preconditions.checkArgument(length >= 0);
    //get block locations for the file range
    LocatedBlocks blockLocations = callGetBlockLocations(namenode, src, 0,
        length);
    if (null == blockLocations) {
      throw new FileNotFoundException("File does not exist: " + src);
    }
    List<LocatedBlock> locatedblocks = blockLocations.getLocatedBlocks();
    final DataOutputBuffer md5out = new DataOutputBuffer();
    int bytesPerCRC = -1;
    DataChecksum.Type crcType = DataChecksum.Type.DEFAULT;
    long crcPerBlock = 0;
    boolean refetchBlocks = false;
    int lastRetriedIndex = -1;

    // get block checksum for each block
    long remaining = length;
    if (src.contains(HdfsConstants.SEPARATOR_DOT_SNAPSHOT_DIR_SEPARATOR)) {
      remaining = Math.min(length, blockLocations.getFileLength());
    }
    for(int i = 0; i < locatedblocks.size() && remaining > 0; i++) {
      if (refetchBlocks) {  // refetch to get fresh tokens
        blockLocations = callGetBlockLocations(namenode, src, 0, length);
        if (null == blockLocations) {
          throw new FileNotFoundException("File does not exist: " + src);
        }
        locatedblocks = blockLocations.getLocatedBlocks();
        refetchBlocks = false;
      }
      LocatedBlock lb = locatedblocks.get(i);
      final ExtendedBlock block = lb.getBlock();
      if (remaining < block.getNumBytes()) {//获取文件的最后一部分，可能不是固定长度
        block.setNumBytes(remaining);
      }
      remaining -= block.getNumBytes();
      final DatanodeInfo[] datanodes = lb.getLocations();
      
      //try each datanode location of the block（有多个DN可以尝试）
      final int timeout = 3000 * datanodes.length + dfsClientConf.socketTimeout;
      boolean done = false;
      for(int j = 0; !done && j < datanodes.length; j++) {
        DataOutputStream out = null;
        DataInputStream in = null;
        
        try {
          //connect to a datanode
          IOStreamPair pair = connectToDN(datanodes[j], timeout, lb);
          out = new DataOutputStream(new BufferedOutputStream(pair.out,
              HdfsConstants.SMALL_BUFFER_SIZE));
          in = new DataInputStream(pair.in);

          if (LOG.isDebugEnabled()) {
            LOG.debug("write to " + datanodes[j] + ": "
                + Op.BLOCK_CHECKSUM + ", block=" + block);
          }
          // get block MD5
          new Sender(out).blockChecksum(block, lb.getBlockToken());

          final BlockOpResponseProto reply =
            BlockOpResponseProto.parseFrom(PBHelper.vintPrefixed(in));

          String logInfo = "for block " + block + " from datanode " + datanodes[j];
          DataTransferProtoUtil.checkBlockOpStatus(reply, logInfo);

          OpBlockChecksumResponseProto checksumData =
            reply.getChecksumResponse();

          //read byte-per-checksum
          final int bpc = checksumData.getBytesPerCrc();
          if (i == 0) { //first block
            bytesPerCRC = bpc;
          }
          else if (bpc != bytesPerCRC) {
            throw new IOException("Byte-per-checksum not matched: bpc=" + bpc
                + " but bytesPerCRC=" + bytesPerCRC);
          }
          
          //read crc-per-block
          final long cpb = checksumData.getCrcPerBlock();
          if (locatedblocks.size() > 1 && i == 0) {
            crcPerBlock = cpb;
          }

          //read md5
          final MD5Hash md5 = new MD5Hash(
              checksumData.getMd5().toByteArray());
          md5.write(md5out);
          
          // read crc-type
          final DataChecksum.Type ct;
          if (checksumData.hasCrcType()) {
            ct = PBHelper.convert(checksumData
                .getCrcType());
          } else {
            LOG.debug("Retrieving checksum from an earlier-version DataNode: " +
                      "inferring checksum by reading first byte");
            ct = inferChecksumTypeByReading(lb, datanodes[j]);
          }

          if (i == 0) { // first block
            crcType = ct;
          } else if (crcType != DataChecksum.Type.MIXED
              && crcType != ct) {
            // if crc types are mixed in a file
            crcType = DataChecksum.Type.MIXED;
          }

          done = true;

          if (LOG.isDebugEnabled()) {
            if (i == 0) {
              LOG.debug("set bytesPerCRC=" + bytesPerCRC
                  + ", crcPerBlock=" + crcPerBlock);
            }
            LOG.debug("got reply from " + datanodes[j] + ": md5=" + md5);
          }
        } catch (InvalidBlockTokenException ibte) {
          if (i > lastRetriedIndex) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Got access token error in response to OP_BLOCK_CHECKSUM "
                  + "for file " + src + " for block " + block
                  + " from datanode " + datanodes[j]
                  + ". Will retry the block once.");
            }
            lastRetriedIndex = i;
            done = true; // actually it's not done; but we'll retry
            i--; // repeat at i-th block
            refetchBlocks = true;
            break;
          }
        } catch (IOException ie) {
          LOG.warn("src=" + src + ", datanodes["+j+"]=" + datanodes[j], ie);
        } finally {
          IOUtils.closeStream(in);
          IOUtils.closeStream(out);
        }
      }

      if (!done) {
        throw new IOException("Fail to get block MD5 for " + block);
      }
    }

    //compute file MD5
    final MD5Hash fileMD5 = MD5Hash.digest(md5out.getData()); 
    switch (crcType) {
      case CRC32:
        return new MD5MD5CRC32GzipFileChecksum(bytesPerCRC,
            crcPerBlock, fileMD5);
      case CRC32C:
        return new MD5MD5CRC32CastagnoliFileChecksum(bytesPerCRC,
            crcPerBlock, fileMD5);
      default:
        // If there is no block allocated for the file,
        // return one with the magic entry that matches what previous
        // hdfs versions return.
        if (locatedblocks.size() == 0) {
          return new MD5MD5CRC32GzipFileChecksum(0, 0, fileMD5);
        }

        // we should never get here since the validity was checked
        // when getCrcType() was called above.
        return null;
    }
  }

  /**
   * Connect to the given datanode's datantrasfer port, and return
   * the resulting IOStreamPair. This includes encryption wrapping, etc.
   *  通过socket连接DN
   */
  private IOStreamPair connectToDN(DatanodeInfo dn, int timeout,
      LocatedBlock lb) throws IOException {
    boolean success = false;
    Socket sock = null;
    try {
      sock = socketFactory.createSocket();
      String dnAddr = dn.getXferAddr(getConf().connectToDnViaHostname);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Connecting to datanode " + dnAddr);
      }
      NetUtils.connect(sock, NetUtils.createSocketAddr(dnAddr), timeout);
      sock.setSoTimeout(timeout);
  
      OutputStream unbufOut = NetUtils.getOutputStream(sock);
      InputStream unbufIn = NetUtils.getInputStream(sock);
      IOStreamPair ret = saslClient.newSocketSend(sock, unbufOut, unbufIn, this,
        lb.getBlockToken(), dn);
      success = true;
      return ret;
    } finally {
      if (!success) {
        IOUtils.closeSocket(sock);
      }
    }
  }
  
  /**
   * Infer the checksum type for a replica by sending an OP_READ_BLOCK
   * for the first byte of that replica. This is used for compatibility
   * with older HDFS versions which did not include the checksum type in
   * OpBlockChecksumResponseProto.
   *
   * @param lb the located block
   * @param dn the connected datanode
   * @return the inferred checksum type
   * @throws IOException if an error occurs
   */
  private Type inferChecksumTypeByReading(LocatedBlock lb, DatanodeInfo dn)
      throws IOException {
    IOStreamPair pair = connectToDN(dn, dfsClientConf.socketTimeout, lb);

    try {
      DataOutputStream out = new DataOutputStream(new BufferedOutputStream(pair.out,
          HdfsConstants.SMALL_BUFFER_SIZE));
      DataInputStream in = new DataInputStream(pair.in);
  
      new Sender(out).readBlock(lb.getBlock(), lb.getBlockToken(), clientName,
          0, 1, true, CachingStrategy.newDefaultStrategy());
      final BlockOpResponseProto reply =
          BlockOpResponseProto.parseFrom(PBHelper.vintPrefixed(in));
      String logInfo = "trying to read " + lb.getBlock() + " from datanode " + dn;
      DataTransferProtoUtil.checkBlockOpStatus(reply, logInfo);

      return PBHelper.convert(reply.getReadOpChecksumInfo().getChecksum().getType());
    } finally {
      IOUtils.cleanup(null, pair.in, pair.out);
    }
  }

  /**
   * Set permissions to a file or directory.
   * @param src path name.
   * @param permission permission to set to
   * 
   * @see ClientProtocol#setPermission(String, FsPermission)
   */
  public void setPermission(String src, FsPermission permission)
      throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("setPermission", src);
    try {
      namenode.setPermission(src, permission);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     SafeModeException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Set file or directory owner.（设置文件所有者）
   * @param src path name.
   * @param username user id.
   * @param groupname user group.
   * 
   * @see ClientProtocol#setOwner(String, String, String)
   */
  public void setOwner(String src, String username, String groupname)
      throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("setOwner", src);
    try {
      namenode.setOwner(src, username, groupname);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     SafeModeException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);                                   
    } finally {
      scope.close();
    }
  }

  private long[] callGetStats() throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("getStats", traceSampler);
    try {
      return namenode.getStats();
    } finally {
      scope.close();
    }
  }

  /**
   * @see ClientProtocol#getStats()
   */
  public FsStatus getDiskStatus() throws IOException {
    long rawNums[] = callGetStats();
    return new FsStatus(rawNums[0], rawNums[1], rawNums[2]);
  }

  /**
   * Returns count of blocks with no good replicas left. Normally should be 
   * zero.(返回缺失块数量-->拥有坏数据的块数量；所有副本都不可用的数据块个数)
   * @throws IOException
   */ 
  public long getMissingBlocksCount() throws IOException {
    return callGetStats()[ClientProtocol.GET_STATS_MISSING_BLOCKS_IDX];
  }
  
  /**
   * Returns count of blocks with replication factor 1 and have
   * lost the only replica.（副本数为1，并且缺失副本的块数）
   * @throws IOException
   */
  public long getMissingReplOneBlocksCount() throws IOException {
    return callGetStats()[ClientProtocol.
        GET_STATS_MISSING_REPL_ONE_BLOCKS_IDX];
  }

  /**
   * Returns count of blocks with one of more replica missing.
   * （有副本缺失的块数量，副本数不足的数据块个数）
   * @throws IOException
   */ 
  public long getUnderReplicatedBlocksCount() throws IOException {
    return callGetStats()[ClientProtocol.GET_STATS_UNDER_REPLICATED_IDX];
  }
  
  /**
   * Returns count of blocks with at least one replica marked corrupt. 
   * 至少有一个副本被标记故障的数量
   * @throws IOException
   */ 
  public long getCorruptBlocksCount() throws IOException {
    return callGetStats()[ClientProtocol.GET_STATS_CORRUPT_BLOCKS_IDX];
  }
  
  
  /**
   * 损坏的文件块列表
   * @return a list in which each entry describes a corrupt file/block
   * @throws IOException
   */
  public CorruptFileBlocks listCorruptFileBlocks(String path,
                                                 String cookie)
        throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("listCorruptFileBlocks", path);
    try {
      return namenode.listCorruptFileBlocks(path, cookie);
    } finally {
      scope.close();
    }
  }

  public DatanodeInfo[] datanodeReport(DatanodeReportType type)
      throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("datanodeReport", traceSampler);
    try {
      return namenode.getDatanodeReport(type);
    } finally {
      scope.close();
    }
  }
    
  public DatanodeStorageReport[] getDatanodeStorageReport(
      DatanodeReportType type) throws IOException {
    checkOpen();
    TraceScope scope =
        Trace.startSpan("datanodeStorageReport", traceSampler);
    try {
      return namenode.getDatanodeStorageReport(type);
    } finally {
      scope.close();
    }
  }

  /**
   * Enter, leave or get safe mode.
   * enter是进入安全模式，leave是离开安全模式，get取得当前安全模式状态
   * @see ClientProtocol#setSafeMode(HdfsConstants.SafeModeAction,boolean)
   */
  public boolean setSafeMode(SafeModeAction action) throws IOException {
    return setSafeMode(action, false);
  }
  
  /**
   * Enter, leave or get safe mode.
   * 
   * @param action
   *          One of SafeModeAction.GET, SafeModeAction.ENTER and
   *          SafeModeActiob.LEAVE
   * @param isChecked
   *          If true, then check only active namenode's safemode status, else
   *          check first namenode's status.
   * @see ClientProtocol#setSafeMode(HdfsConstants.SafeModeAction, boolean)
   */
  public boolean setSafeMode(SafeModeAction action, boolean isChecked) throws IOException{
    TraceScope scope =
        Trace.startSpan("setSafeMode", traceSampler);
    try {
      return namenode.setSafeMode(action, isChecked);
    } finally {
      scope.close();
    }
  }
 
  /**
   * Create one snapshot.
   * 创建快照
   * @param snapshotRoot The directory where the snapshot is to be taken
   * @param snapshotName Name of the snapshot
   * @return the snapshot path.
   * @see ClientProtocol#createSnapshot(String, String)
   */
  public String createSnapshot(String snapshotRoot, String snapshotName)
      throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("createSnapshot", traceSampler);
    try {
      return namenode.createSnapshot(snapshotRoot, snapshotName);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }
  
  /**
   * Delete a snapshot of a snapshottable directory.
   * 删除可快照目录的快照
   * @param snapshotRoot The snapshottable directory that the 
   *                    to-be-deleted snapshot belongs to
   * @param snapshotName The name of the to-be-deleted snapshot
   * @throws IOException
   * @see ClientProtocol#deleteSnapshot(String, String)
   */
  public void deleteSnapshot(String snapshotRoot, String snapshotName)
      throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("deleteSnapshot", traceSampler);
    try {
      namenode.deleteSnapshot(snapshotRoot, snapshotName);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }
  
  /**
   * Rename a snapshot.（快照重命令）
   * @param snapshotDir The directory path where the snapshot was taken
   * @param snapshotOldName Old name of the snapshot
   * @param snapshotNewName New name of the snapshot
   * @throws IOException
   * @see ClientProtocol#renameSnapshot(String, String, String)
   */
  public void renameSnapshot(String snapshotDir, String snapshotOldName,
      String snapshotNewName) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("renameSnapshot", traceSampler);
    try {
      namenode.renameSnapshot(snapshotDir, snapshotOldName, snapshotNewName);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }
  
  /**
   * Get all the current snapshottable directories.
   * 获取所有可以快照的目录列表
   * @return All the current snapshottable directories
   * @throws IOException
   * @see ClientProtocol#getSnapshottableDirListing()
   */
  public SnapshottableDirectoryStatus[] getSnapshottableDirListing()
      throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("getSnapshottableDirListing",
        traceSampler);
    try {
      return namenode.getSnapshottableDirListing();
    } catch(RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }

  /**
   * Allow snapshot on a directory.
   * 设置一个目录允许快照
   * @see ClientProtocol#allowSnapshot(String snapshotRoot)
   */
  public void allowSnapshot(String snapshotRoot) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("allowSnapshot", traceSampler);
    try {
      namenode.allowSnapshot(snapshotRoot);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }
  
  /**
   * Disallow snapshot on a directory.
   * 设置目录不允许快照
   * @see ClientProtocol#disallowSnapshot(String snapshotRoot)
   */
  public void disallowSnapshot(String snapshotRoot) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("disallowSnapshot", traceSampler);
    try {
      namenode.disallowSnapshot(snapshotRoot);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }
  
  /**
   * Get the difference between two snapshots, or between a snapshot and the
   * current tree of a directory.（获取快照差异）
   * @see ClientProtocol#getSnapshotDiffReport(String, String, String)
   */
  public SnapshotDiffReport getSnapshotDiffReport(String snapshotDir,
      String fromSnapshot, String toSnapshot) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("getSnapshotDiffReport", traceSampler);
    try {
      return namenode.getSnapshotDiffReport(snapshotDir,
          fromSnapshot, toSnapshot);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }

 //添加缓存指示
  public long addCacheDirective(
      CacheDirectiveInfo info, EnumSet<CacheFlag> flags) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("addCacheDirective", traceSampler);
    try {
      return namenode.addCacheDirective(info, flags);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }
  
  public void modifyCacheDirective(
      CacheDirectiveInfo info, EnumSet<CacheFlag> flags) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("modifyCacheDirective", traceSampler);
    try {
      namenode.modifyCacheDirective(info, flags);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }

  public void removeCacheDirective(long id)
      throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("removeCacheDirective", traceSampler);
    try {
      namenode.removeCacheDirective(id);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }
  
//缓存指示列表
  public RemoteIterator<CacheDirectiveEntry> listCacheDirectives(
      CacheDirectiveInfo filter) throws IOException {
    return new CacheDirectiveIterator(namenode, filter, traceSampler);
  }

  public void addCachePool(CachePoolInfo info) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("addCachePool", traceSampler);
    try {
      namenode.addCachePool(info);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }

  public void modifyCachePool(CachePoolInfo info) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("modifyCachePool", traceSampler);
    try {
      namenode.modifyCachePool(info);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }

  public void removeCachePool(String poolName) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("removeCachePool", traceSampler);
    try {
      namenode.removeCachePool(poolName);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException();
    } finally {
      scope.close();
    }
  }

  public RemoteIterator<CachePoolEntry> listCachePools() throws IOException {
    return new CachePoolIterator(namenode, traceSampler);
  }

  /**
   * Save namespace image.
   * 
   * @see ClientProtocol#saveNamespace()
   */
  void saveNamespace() throws AccessControlException, IOException {
    TraceScope scope = Trace.startSpan("saveNamespace", traceSampler);
    try {
      namenode.saveNamespace();
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Rolls the edit log on the active NameNode.
   * 回滚editlog，即创建一个新的editlog
   * @return the txid of the new log segment 
   *
   * @see ClientProtocol#rollEdits()
   */
  long rollEdits() throws AccessControlException, IOException {
    TraceScope scope = Trace.startSpan("rollEdits", traceSampler);
    try {
      return namenode.rollEdits();
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class);
    } finally {
      scope.close();
    }
  }

  @VisibleForTesting
  ExtendedBlock getPreviousBlock(long fileId) {
    return filesBeingWritten.get(fileId).getBlock();
  }
  
  /**
   * enable/disable restore failed storage.
   * 
   * @see ClientProtocol#restoreFailedStorage(String arg)
   */
  boolean restoreFailedStorage(String arg)
      throws AccessControlException, IOException{
    TraceScope scope = Trace.startSpan("restoreFailedStorage", traceSampler);
    try {
      return namenode.restoreFailedStorage(arg);
    } finally {
      scope.close();
    }
  }

  /**
   * Refresh the hosts and exclude files.  (Rereads them.)
   * See {@link ClientProtocol#refreshNodes()} 
   * for more details.
   * 刷新集群中的主机节点列表，当向集群中添加节点或者从集群中移除节点时，需要执行
   * @see ClientProtocol#refreshNodes()
   */
  public void refreshNodes() throws IOException {
    TraceScope scope = Trace.startSpan("refreshNodes", traceSampler);
    try {
      namenode.refreshNodes();
    } finally {
      scope.close();
    }
  }

  /**
   * Dumps DFS data structures into specified file.
   * 将 NN中的元数据保存到指定的文件
   * @see ClientProtocol#metaSave(String)
   */
  public void metaSave(String pathname) throws IOException {
    TraceScope scope = Trace.startSpan("metaSave", traceSampler);
    try {
      namenode.metaSave(pathname);
    } finally {
      scope.close();
    }
  }

  /**
   * Requests the namenode to tell all datanodes to use a new, non-persistent
   * bandwidth value for dfs.balance.bandwidthPerSec.
   * See {@link ClientProtocol#setBalancerBandwidth(long)} 
   * for more details.
   * 设置均衡带宽
   * @see ClientProtocol#setBalancerBandwidth(long)
   */
  public void setBalancerBandwidth(long bandwidth) throws IOException {
    TraceScope scope = Trace.startSpan("setBalancerBandwidth", traceSampler);
    try {
      namenode.setBalancerBandwidth(bandwidth);
    } finally {
      scope.close();
    }
  }
    
  
 /**
  * 文件系统的完成升级？
  * @see ClientProtocol#finalizeUpgrade()
  */
  public void finalizeUpgrade() throws IOException {
    TraceScope scope = Trace.startSpan("finalizeUpgrade", traceSampler);
    try {
      namenode.finalizeUpgrade();
    } finally {
      scope.close();
    }
  }

  RollingUpgradeInfo rollingUpgrade(RollingUpgradeAction action) throws IOException {
    TraceScope scope = Trace.startSpan("rollingUpgrade", traceSampler);
    try {
      return namenode.rollingUpgrade(action);
    } finally {
      scope.close();
    }
  }

  /**
   */
  @Deprecated
  public boolean mkdirs(String src) throws IOException {
    return mkdirs(src, null, true);
  }

  /**
   * Create a directory (or hierarchy of directories) with the given
   * name and permission.
   *
   * @param src The path of the directory being created
   * @param permission The permission of the directory being created.
   * If permission == null, use {@link FsPermission#getDefault()}.
   * @param createParent create missing parent directory if true
   * 
   * @return True if the operation success.
   * 
   * @see ClientProtocol#mkdirs(String, FsPermission, boolean)
   */
  public boolean mkdirs(String src, FsPermission permission,
      boolean createParent) throws IOException {
    if (permission == null) {
      permission = FsPermission.getDefault();
    }
    FsPermission masked = permission.applyUMask(dfsClientConf.uMask);
    return primitiveMkdir(src, masked, createParent);
  }

  /**
   * Same {{@link #mkdirs(String, FsPermission, boolean)} except
   * that the permissions has already been masked against umask.
   */
  public boolean primitiveMkdir(String src, FsPermission absPermission)
    throws IOException {
    return primitiveMkdir(src, absPermission, true);
  }

  /**
   * Same {{@link #mkdirs(String, FsPermission, boolean)} except
   * that the permissions has already been masked against umask.
   */
  public boolean primitiveMkdir(String src, FsPermission absPermission, 
    boolean createParent)
    throws IOException {
    checkOpen();
    if (absPermission == null) {
      absPermission = 
        FsPermission.getDefault().applyUMask(dfsClientConf.uMask);
    } 

    if(LOG.isDebugEnabled()) {
      LOG.debug(src + ": masked=" + absPermission);
    }
    TraceScope scope = Trace.startSpan("mkdir", traceSampler);
    try {
      return namenode.mkdirs(src, absPermission, createParent);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     InvalidPathException.class,
                                     FileAlreadyExistsException.class,
                                     FileNotFoundException.class,
                                     ParentNotDirectoryException.class,
                                     SafeModeException.class,
                                     NSQuotaExceededException.class,
                                     DSQuotaExceededException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }
  
  /**
   * Get {@link ContentSummary} rooted at the specified directory.
   * @param src The string representation of the path
   * 
   * @see ClientProtocol#getContentSummary(String)
   */
  ContentSummary getContentSummary(String src) throws IOException {
    TraceScope scope = getPathTraceScope("getContentSummary", src);
    try {
      return namenode.getContentSummary(src);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Sets or resets quotas for a directory.设置配额信息
   * @see ClientProtocol#setQuota(String, long, long, StorageType)
   */
  void setQuota(String src, long namespaceQuota, long storagespaceQuota)
      throws IOException {
    // sanity check
    if ((namespaceQuota <= 0 && namespaceQuota != HdfsConstants.QUOTA_DONT_SET &&
         namespaceQuota != HdfsConstants.QUOTA_RESET) ||
        (storagespaceQuota <= 0 && storagespaceQuota != HdfsConstants.QUOTA_DONT_SET &&
         storagespaceQuota != HdfsConstants.QUOTA_RESET)) {
      throw new IllegalArgumentException("Invalid values for quota : " +
                                         namespaceQuota + " and " +
                                         storagespaceQuota);
                                         
    }
    TraceScope scope = getPathTraceScope("setQuota", src);
    try {
      // Pass null as storage type for traditional namespace/storagespace quota.
      namenode.setQuota(src, namespaceQuota, storagespaceQuota, null);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     DSQuotaExceededException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * Sets or resets quotas by storage type for a directory.
   * @see ClientProtocol#setQuota(String, long, long, StorageType)
   */
  void setQuotaByStorageType(String src, StorageType type, long quota)
      throws IOException {
    if (quota <= 0 && quota != HdfsConstants.QUOTA_DONT_SET &&
        quota != HdfsConstants.QUOTA_RESET) {
      throw new IllegalArgumentException("Invalid values for quota :" +
        quota);
    }
    if (type == null) {
      throw new IllegalArgumentException("Invalid storage type(null)");
    }
    if (!type.supportTypeQuota()) {
      throw new IllegalArgumentException("Don't support Quota for storage type : "
        + type.toString());
    }
    TraceScope scope = getPathTraceScope("setQuotaByStorageType", src);
    try {
      namenode.setQuota(src, HdfsConstants.QUOTA_DONT_SET, quota, type);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
        FileNotFoundException.class,
        QuotaByStorageTypeExceededException.class,
        UnresolvedPathException.class,
        SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }
  /**
   * set the modification and access time of a file
   * 设置修改时间和访问时间
   * @see ClientProtocol#setTimes(String, long, long)
   */
  public void setTimes(String src, long mtime, long atime) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("setTimes", src);
    try {
      namenode.setTimes(src, mtime, atime);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class,
                                     SnapshotAccessControlException.class);
    } finally {
      scope.close();
    }
  }

  /**
   * @deprecated use {@link HdfsDataInputStream} instead.
   */
  @Deprecated
  public static class DFSDataInputStream extends HdfsDataInputStream {

    public DFSDataInputStream(DFSInputStream in) throws IOException {
      super(in);
    }
  }

  void reportChecksumFailure(String file, ExtendedBlock blk, DatanodeInfo dn) {
    DatanodeInfo [] dnArr = { dn };
    LocatedBlock [] lblocks = { new LocatedBlock(blk, dnArr) };
    reportChecksumFailure(file, lblocks);
  }
    
  // just reports checksum failure and ignores any exception during the report.
  void reportChecksumFailure(String file, LocatedBlock lblocks[]) {
    try {
      reportBadBlocks(lblocks);//修复读取损坏的块
    } catch (IOException ie) {
      LOG.info("Found corruption while reading " + file
          + ". Error repairing corrupt blocks. Bad blocks remain.", ie);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[clientName=" + clientName
        + ", ugi=" + ugi + "]"; 
  }

  public CachingStrategy getDefaultReadCachingStrategy() {
    return defaultReadCachingStrategy;
  }

  public CachingStrategy getDefaultWriteCachingStrategy() {
    return defaultWriteCachingStrategy;
  }

  public ClientContext getClientContext() {
    return clientContext;
  }

  public void modifyAclEntries(String src, List<AclEntry> aclSpec)
      throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("modifyAclEntries", src);
    try {
      namenode.modifyAclEntries(src, aclSpec);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     AclException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     SafeModeException.class,
                                     SnapshotAccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public void removeAclEntries(String src, List<AclEntry> aclSpec)
      throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("removeAclEntries", traceSampler);
    try {
      namenode.removeAclEntries(src, aclSpec);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     AclException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     SafeModeException.class,
                                     SnapshotAccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public void removeDefaultAcl(String src) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("removeDefaultAcl", traceSampler);
    try {
      namenode.removeDefaultAcl(src);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     AclException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     SafeModeException.class,
                                     SnapshotAccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public void removeAcl(String src) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("removeAcl", traceSampler);
    try {
      namenode.removeAcl(src);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     AclException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     SafeModeException.class,
                                     SnapshotAccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public void setAcl(String src, List<AclEntry> aclSpec) throws IOException {
    checkOpen();
    TraceScope scope = Trace.startSpan("setAcl", traceSampler);
    try {
      namenode.setAcl(src, aclSpec);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     AclException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     SafeModeException.class,
                                     SnapshotAccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public AclStatus getAclStatus(String src) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("getAclStatus", src);
    try {
      return namenode.getAclStatus(src);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     AclException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }
  
  public void createEncryptionZone(String src, String keyName)
    throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("createEncryptionZone", src);
    try {
      namenode.createEncryptionZone(src, keyName);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     SafeModeException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public EncryptionZone getEZForPath(String src)
          throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("getEZForPath", src);
    try {
      return namenode.getEZForPath(src);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public RemoteIterator<EncryptionZone> listEncryptionZones()
      throws IOException {
    checkOpen();
    return new EncryptionZoneIterator(namenode, traceSampler);
  }

  public void setXAttr(String src, String name, byte[] value, 
      EnumSet<XAttrSetFlag> flag) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("setXAttr", src);
    try {
      namenode.setXAttr(src, XAttrHelper.buildXAttr(name, value), flag);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     SafeModeException.class,
                                     SnapshotAccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }
  
  //有同名的返回第一个xattr
  public byte[] getXAttr(String src, String name) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("getXAttr", src);
    try {
      final List<XAttr> xAttrs = XAttrHelper.buildXAttrAsList(name);
      final List<XAttr> result = namenode.getXAttrs(src, xAttrs);
      return XAttrHelper.getFirstXAttrValue(result);
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }
  
  public Map<String, byte[]> getXAttrs(String src) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("getXAttrs", src);
    try {
      return XAttrHelper.buildXAttrMap(namenode.getXAttrs(src, null));
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }
  
  public Map<String, byte[]> getXAttrs(String src, List<String> names) 
      throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("getXAttrs", src);
    try {
      return XAttrHelper.buildXAttrMap(namenode.getXAttrs(
          src, XAttrHelper.buildXAttrs(names)));
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }
  
  public List<String> listXAttrs(String src)
          throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("listXAttrs", src);
    try {
      final Map<String, byte[]> xattrs =
        XAttrHelper.buildXAttrMap(namenode.listXAttrs(src));
      return Lists.newArrayList(xattrs.keySet());
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public void removeXAttr(String src, String name) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("removeXAttr", src);
    try {
      namenode.removeXAttr(src, XAttrHelper.buildXAttr(name));
    } catch(RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
                                     FileNotFoundException.class,
                                     NSQuotaExceededException.class,
                                     SafeModeException.class,
                                     SnapshotAccessControlException.class,
                                     UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public void checkAccess(String src, FsAction mode) throws IOException {
    checkOpen();
    TraceScope scope = getPathTraceScope("checkAccess", src);
    try {
      namenode.checkAccess(src, mode);
    } catch (RemoteException re) {
      throw re.unwrapRemoteException(AccessControlException.class,
          FileNotFoundException.class,
          UnresolvedPathException.class);
    } finally {
      scope.close();
    }
  }

  public DFSInotifyEventInputStream getInotifyEventStream() throws IOException {
    return new DFSInotifyEventInputStream(traceSampler, namenode);
  }

  public DFSInotifyEventInputStream getInotifyEventStream(long lastReadTxid)
      throws IOException {
    return new DFSInotifyEventInputStream(traceSampler, namenode, lastReadTxid);
  }

  @Override // RemotePeerFactory
  public Peer newConnectedPeer(InetSocketAddress addr,
      Token<BlockTokenIdentifier> blockToken, DatanodeID datanodeId)
      throws IOException {
    Peer peer = null;
    boolean success = false;
    Socket sock = null;
    try {
      sock = socketFactory.createSocket();
      NetUtils.connect(sock, addr,
        getRandomLocalInterfaceAddr(),
        dfsClientConf.socketTimeout);
      peer = TcpPeerServer.peerFromSocketAndKey(saslClient, sock, this,
          blockToken, datanodeId);
      peer.setReadTimeout(dfsClientConf.socketTimeout);
      success = true;
      return peer;
    } finally {
      if (!success) {
        IOUtils.cleanup(LOG, peer);
        IOUtils.closeSocket(sock);
      }
    }
  }

  /**
   * Create hedged reads thread pool, HEDGED_READ_THREAD_POOL, if
   * it does not already exist.
   * @param num Number of threads for hedged reads thread pool.
   * If zero, skip hedged reads thread pool creation.
   */
  private synchronized void initThreadsNumForHedgedReads(int num) {
    if (num <= 0 || HEDGED_READ_THREAD_POOL != null) return;
    HEDGED_READ_THREAD_POOL = new ThreadPoolExecutor(1, num, 60,
        TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
        new Daemon.DaemonFactory() {
          private final AtomicInteger threadIndex =
            new AtomicInteger(0); 
          @Override
          public Thread newThread(Runnable r) {
            Thread t = super.newThread(r);
            t.setName("hedgedRead-" +
              threadIndex.getAndIncrement());
            return t;
          }
        },
        new ThreadPoolExecutor.CallerRunsPolicy() {

      @Override
      public void rejectedExecution(Runnable runnable,
          ThreadPoolExecutor e) {
        LOG.info("Execution rejected, Executing in current thread");
        HEDGED_READ_METRIC.incHedgedReadOpsInCurThread();
        // will run in the current thread
        super.rejectedExecution(runnable, e);
      }
    });
    HEDGED_READ_THREAD_POOL.allowCoreThreadTimeOut(true);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Using hedged reads; pool threads=" + num);
    }
  }

  long getHedgedReadTimeout() {
    return this.hedgedReadThresholdMillis;
  }

  @VisibleForTesting
  void setHedgedReadTimeout(long timeoutMillis) {
    this.hedgedReadThresholdMillis = timeoutMillis;
  }

  ThreadPoolExecutor getHedgedReadsThreadPool() {
    return HEDGED_READ_THREAD_POOL;
  }

  boolean isHedgedReadsEnabled() {
    return (HEDGED_READ_THREAD_POOL != null) &&
      HEDGED_READ_THREAD_POOL.getMaximumPoolSize() > 0;
  }

  DFSHedgedReadMetrics getHedgedReadMetrics() {
    return HEDGED_READ_METRIC;
  }

  public KeyProvider getKeyProvider() {
    return clientContext.getKeyProviderCache().get(conf);
  }

  @VisibleForTesting
  public void setKeyProvider(KeyProvider provider) {
    try {
      clientContext.getKeyProviderCache().setKeyProvider(conf, provider);
    } catch (IOException e) {
     LOG.error("Could not set KeyProvider !!", e);
    }
  }

  /**
   * Probe for encryption enabled on this filesystem.
   * See {@link DFSUtil#isHDFSEncryptionEnabled(Configuration)}
   * @return true if encryption is enabled
   */
  public boolean isHDFSEncryptionEnabled() {
    return DFSUtil.isHDFSEncryptionEnabled(this.conf);
  }

  /**
   * Returns the SaslDataTransferClient configured for this DFSClient.
   *
   * @return SaslDataTransferClient configured for this DFSClient
   */
  public SaslDataTransferClient getSaslDataTransferClient() {
    return saslClient;
  }

  private static final byte[] PATH = "path".getBytes(Charset.forName("UTF-8"));

  TraceScope getPathTraceScope(String description, String path) {
    TraceScope scope = Trace.startSpan(description, traceSampler);
    Span span = scope.getSpan();
    if (span != null) {
      if (path != null) {
        span.addKVAnnotation(PATH,
            path.getBytes(Charset.forName("UTF-8")));
      }
    }
    return scope;
  }

//将参数转化为UTF-8
  private static final byte[] SRC = "src".getBytes(Charset.forName("UTF-8"));

  private static final byte[] DST = "dst".getBytes(Charset.forName("UTF-8"));

  TraceScope getSrcDstTraceScope(String description, String src, String dst) {
    TraceScope scope = Trace.startSpan(description, traceSampler);
    Span span = scope.getSpan();
    if (span != null) {
      if (src != null) {
        span.addKVAnnotation(SRC,
            src.getBytes(Charset.forName("UTF-8")));
      }
      if (dst != null) {
        span.addKVAnnotation(DST,
            dst.getBytes(Charset.forName("UTF-8")));
      }
    }
    return scope;
  }


}
