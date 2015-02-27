/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stacksync.syncservice.db.infinispan;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.stacksync.syncservice.db.infinispan.models.ChunkRMI;
import com.stacksync.syncservice.db.infinispan.models.DeviceRMI;
import com.stacksync.syncservice.db.infinispan.models.ItemMetadataRMI;
import com.stacksync.syncservice.db.infinispan.models.ItemRMI;
import com.stacksync.syncservice.db.infinispan.models.ItemVersionRMI;
import com.stacksync.syncservice.db.infinispan.models.UserRMI;
import com.stacksync.syncservice.db.infinispan.models.UserWorkspaceRMI;
import com.stacksync.syncservice.db.infinispan.models.WorkspaceRMI;

import org.infinispan.atomic.AtomicObjectFactory;

/**
 *
 * @author Laura Martínez Sanahuja <lauramartinezsanahuja@gmail.com>
 */
public class InfinispanDAO implements InfinispanWorkspaceDAO, InfinispanItemDAO, InfinispanItemVersionDAO, InfinispanUserDAO, InfinispanDeviceDAO, Serializable {

    private WorkspaceRMI workspace;
    private UserRMI user;
    private final AtomicObjectFactory factory;

    public InfinispanDAO(AtomicObjectFactory factory) {

        this.factory = factory;

    }

    //************************************
    //************************************
    //************* WORKSPACE ************
    //************************************
    //************************************
    @Override
    public WorkspaceRMI getById(UUID id) throws RemoteException {

        workspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, id.toString(), false, null, false);
        return workspace;

    }

    @Override
    public List<WorkspaceRMI> getByUserId(UUID userId) throws RemoteException {

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, userId.toString(), false, null, false);
        List<UUID> list = user.getWorkspaces();

        List<WorkspaceRMI> result = new ArrayList<WorkspaceRMI>();

        for (UUID id : list) {
            workspace = getById(id);
            if (workspace.getId() != null) {
                result.add(workspace);
            }
        }

        return result;

    }

    @Override
    public WorkspaceRMI getDefaultWorkspaceByUserId(UUID userId) throws RemoteException {

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, userId.toString(), false, null, false);
        List<UUID> workspaces = user.getWorkspaces();

        WorkspaceRMI defaultWorkspace = null;
        for (UUID w : workspaces) {
            WorkspaceRMI wspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, w.toString(), false, null, false);
            if (!wspace.isShared()) {
                defaultWorkspace = wspace;
                break;
            }
        }
        return defaultWorkspace;

    }

    @Override
    public WorkspaceRMI getByItemId(Long itemId) throws RemoteException {
        //Senseless

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public void add(WorkspaceRMI wspace) throws RemoteException {
        //Maybe it should be named create

        workspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, wspace.getId().toString(), false, null, false);
        workspace.setWorkspace(wspace);
        factory.disposeInstanceOf(WorkspaceRMI.class, wspace.getId().toString(), true);

    }

    @Override
    public void update(UserRMI usr, WorkspaceRMI wspace) throws RemoteException {

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, usr.getId().toString(), false, null, false);

        List<UUID> list = user.getWorkspaces();

        for (UUID w : list) {
            if (w.equals(wspace.getId())) {
                factory.disposeInstanceOf(WorkspaceRMI.class, w.toString(), true);
            }
        }

    }

    @Override
    public void deleteWorkspace(UUID id) throws RemoteException {

        workspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, id.toString(), false, null, false);

        List<UUID> list = workspace.getUsers();

        for (UUID u : list) {
            user = (UserRMI) factory.getInstanceOf(UserRMI.class, u.toString(), false, null, false);
            user.removeWorkspace(id);
            factory.disposeInstanceOf(UserRMI.class, u.toString(), true);
        }

        factory.disposeInstanceOf(WorkspaceRMI.class, id.toString(), false);

    }

    @Override
    public void addUser(UserRMI usr, WorkspaceRMI wspace) throws RemoteException {

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, usr.getId().toString(), false, null, false);
        workspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, wspace.getId().toString(), false, null, false);

        user.addWorkspace(workspace.getId());
        workspace.addUser(user.getId());

        factory.disposeInstanceOf(UserRMI.class, usr.getId().toString(), true);
        factory.disposeInstanceOf(UserRMI.class, wspace.getId().toString(), true);

    }

    @Override
    public void deleteUser(UserRMI usr, WorkspaceRMI wspace) throws RemoteException {

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, usr.getId().toString(), false, null, false);
        workspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, wspace.getId().toString(), false, null, false);

        workspace.removeUser(usr.getId());
        user.removeWorkspace(wspace.getId());

        factory.disposeInstanceOf(UserRMI.class, usr.getId().toString(), true);
        factory.disposeInstanceOf(UserRMI.class, wspace.getId().toString(), true);

    }

    @Override
    public List<UserWorkspaceRMI> getMembersById(UUID wspaceId) throws RemoteException {

        workspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, wspaceId.toString(), false, null, false);

        List<UUID> usrs = workspace.getUsers();
        UUID owner = workspace.getOwner();
        if (!usrs.contains(owner)) {
            usrs.add(owner);
            workspace.addUser(owner);
        }
        List<UserWorkspaceRMI> result = new ArrayList<UserWorkspaceRMI>();
        UserWorkspaceRMI usrwspace;

        for (UUID uuid : usrs) {
            user = (UserRMI) factory.getInstanceOf(UserRMI.class, uuid.toString(), false, null, false);
            usrwspace = new UserWorkspaceRMI(user, workspace);
            result.add(usrwspace);
        }

        return result;

    }

    //************************************
    //************************************
    //*************** ITEM ***************
    //************************************
    //************************************
    @Override
    public ItemRMI findById(Long id) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI i : items) {
            if (i.getId().equals(id)) {
                return i;
            }
        }

        return null;

    }

    @Override
    public void add(ItemRMI item) throws RemoteException {

        workspace.addItem(item);

    }

    @Override
    public void update(ItemRMI item) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI i : items) {
            if (i.getId().equals(item.getId())) {
                items.remove(i);
                items.add(item);
                break;
            }
        }

        workspace.setItems(items);
        factory.disposeInstanceOf(WorkspaceRMI.class, workspace.getId().toString(), true);

    }

    @Override
    public void put(ItemRMI item) throws RemoteException {

        boolean exist = false;
        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI i : items) {
            if (i.getId().equals(item.getId())) {
                update(item);
                exist = true;
                break;
            }
        }

        if (!exist) {
            add(item);
        }

    }

    @Override
    public void delete(Long id) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI i : items) {
            if (i.getId().equals(id)) {
                items.remove(i);
                break;
            }
        }

    }

    private ItemMetadataRMI getItemMetadataFromItem(ItemRMI item) {

        return getItemMetadataFromItem(item, item.getLatestVersionNumber(), false, false, false);

    }

    private ItemMetadataRMI getItemMetadataFromItem(ItemRMI item, Long version, Boolean includeList, Boolean includeDeleted, Boolean includeChunks) {

        ItemMetadataRMI itemMetadata = null;
        Long test = 5L;

        List<ItemVersionRMI> versions = item.getVersions();
        for (ItemVersionRMI itemVersion : versions) {
            if (itemVersion.getVersion().equals(version)) {
                itemMetadata = createItemMetadataFromItemAndItemVersion(item, itemVersion, includeChunks);
                if (includeList && item.isFolder()) {
                    // Get children :D
                    itemMetadata = addChildrenFromItemMetadata(itemMetadata, includeDeleted);
                }
                break;
            }
        }

        return itemMetadata;

    }

    private ItemMetadataRMI addChildrenFromItemMetadata(ItemMetadataRMI itemMetadata, Boolean includeDeleted) {

        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI thisItem : items) {
            if (itemMetadata.getId().equals(thisItem.getParentId()) && ((includeDeleted && itemMetadata.getStatus().equals("DELETED")) || !itemMetadata.getStatus().equals("DELETED"))) {
                ItemVersionRMI thisItemVersion = thisItem.getLatestVersion();
                ItemMetadataRMI child = createItemMetadataFromItemAndItemVersion(thisItem, thisItemVersion);
                itemMetadata.addChild(child);
            }
        }

        return itemMetadata;

    }

    private ItemMetadataRMI createItemMetadataFromItemAndItemVersion(ItemRMI item, ItemVersionRMI itemVersion) {

        return createItemMetadataFromItemAndItemVersion(item, itemVersion, false);

    }

    private ItemMetadataRMI createItemMetadataFromItemAndItemVersion(ItemRMI item, ItemVersionRMI itemVersion, Boolean includeChunks) {

        ArrayList<String> chunks = new ArrayList<String>();
        if (includeChunks) {
            for (ChunkRMI chunk : itemVersion.getChunks()) {
                chunks.add(chunk.toString());
            }
        } else {
            chunks = null;
        }

        return new ItemMetadataRMI(item.getId(), itemVersion.getVersion(), itemVersion.getDevice().getId(), item.getParentId(), item.getClientParentFileVersion(), itemVersion.getStatus(), itemVersion.getModifiedAt(), itemVersion.getChecksum(), itemVersion.getSize(), item.isFolder(), item.getFilename(), item.getMimetype(), chunks);

    }

    @Override
    public List<ItemMetadataRMI> getItemsByWorkspaceId(UUID workspaceId) throws RemoteException {

        List<ItemMetadataRMI> result = new ArrayList<ItemMetadataRMI>();
        ItemMetadataRMI itemMetadata;

        workspace = getById(workspaceId);

        List<ItemRMI> list = workspace.getItems();

        for (ItemRMI item : list) {
            itemMetadata = getItemMetadataFromItem(item);
            if (itemMetadata != null) {
                result.add(itemMetadata);
            }
        }

        return result;

    }

    @Override
    public List<ItemMetadataRMI> getItemsById(Long id) throws RemoteException {

        List<ItemMetadataRMI> result = new ArrayList<ItemMetadataRMI>();
        ItemMetadataRMI itemMetadata;

        List<ItemRMI> list = workspace.getItems();

        for (ItemRMI item : list) {
            if (item.getId().equals(id) || (item.getParentId() != null && item.getParentId().equals(id))) {
                itemMetadata = getItemMetadataFromItem(item);
                if (itemMetadata != null) {
                    result.add(itemMetadata);
                }
            }
        }

        return result;

    }

    @Override
    public ItemMetadataRMI findById(Long id, Boolean includeList, Long version, Boolean includeDeleted, Boolean includeChunks) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();

        ItemMetadataRMI itemMetadata = null;
        for (ItemRMI item : items) {
            if (item.getId().equals(id)) {
                itemMetadata = getItemMetadataFromItem(item, version, includeList, includeDeleted, includeChunks);
                break;
            }
        }

        return itemMetadata;

    }

    @Override
    public ItemMetadataRMI findByUserId(UUID serverUserId, Boolean includeDeleted) throws RemoteException {
        //Senseless

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public ItemMetadataRMI findItemVersionsById(Long id) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();
        ItemMetadataRMI itemMetadata = null;
        ItemRMI item = null;

        for (ItemRMI currentItem : items) {
            if (currentItem.getId().equals(id)) {
                item = currentItem;
            }
        }

        if (item == null) {
            return null;
        }

        for (ItemVersionRMI itemVersion : item.getVersions()) {
            if (itemVersion.getVersion().equals(item.getLatestVersionNumber())) {
                itemMetadata = createItemMetadataFromItemAndItemVersion(item, itemVersion);
                break;
            }
        }

        if (itemMetadata == null) {
            return null;
        }

        for (ItemVersionRMI itemVersion : item.getVersions()) {
            if (!itemVersion.getVersion().equals(item.getLatestVersionNumber())) {
                ItemMetadataRMI version = createItemMetadataFromItemAndItemVersion(item, itemVersion);
                if (version != null) {
                    itemMetadata.addChild(version);
                }
            }
        }

        return itemMetadata;

    }

    @Override
    public List<String> migrateItem(Long itemId, UUID workspaceId) throws RemoteException {
        //Senseless

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    //************************************
    //************************************
    //************ ITEMVERSION ***********
    //************************************
    //************************************
    @Override
    public ItemMetadataRMI findByItemIdAndVersion(Long id, Long version) throws RemoteException {

        return findById(id, Boolean.FALSE, version, Boolean.FALSE, Boolean.FALSE);

    }

    @Override
    public void add(ItemVersionRMI itemVersion) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI item : items) {
            if (item.getId().equals(itemVersion.getItemId())) {
                item.addVersion(itemVersion);
                item.setLatestVersionNumber(itemVersion.getVersion());
                break;
            }
        }

    }

    @Override
    public void insertChunk(Long itemVersionId, Long chunkId, Integer order) throws RemoteException {
        //Senseless

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public void insertChunks(List<ChunkRMI> chunks, long itemVersionId) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();
        List<ItemVersionRMI> versions;

        for (ItemRMI item : items) {
            versions = item.getVersions();
            for (ItemVersionRMI version : versions) {
                if (version.getId().equals(itemVersionId)) {
                    version.setChunks(chunks);
                }
            }
        }

    }

    @Override
    public List<ChunkRMI> findChunks(Long itemVersionId) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();
        List<ItemVersionRMI> versions;

        for (ItemRMI item : items) {
            versions = item.getVersions();
            for (ItemVersionRMI version : versions) {
                if (version.getId().equals(itemVersionId)) {
                    return version.getChunks();
                }
            }
        }

        return null;

    }

    @Override
    public void update(ItemVersionRMI itemVersion) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI item : items) {
            if (item.getId().equals(itemVersion.getItemId())) {
                List<ItemVersionRMI> versions = item.getVersions();
                for (ItemVersionRMI version : versions) {
                    if (version.getVersion().equals(itemVersion.getVersion())) {
                        item.removeVersion(version);
                        item.addVersion(itemVersion);
                        break;
                    }
                }
                break;
            }
        }

    }

    @Override
    public void delete(ItemVersionRMI itemVersion) throws RemoteException {

        List<ItemRMI> items = workspace.getItems();

        for (ItemRMI item : items) {
            if (item.getId().equals(itemVersion.getItemId())) {
                item.removeVersion(itemVersion);
                if (item.getLatestVersionNumber().equals(itemVersion.getVersion())) {
                    item.setLatestVersionNumber(itemVersion.getVersion() - 1L);
                }
                break;
            }
        }

    }

    //************************************
    //************************************
    //*************** USER ***************
    //************************************
    //************************************
    @Override
    public UserRMI findById(UUID id) throws RemoteException {

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, id.toString(), false, null, false);
        return user;

    }

    @Override
    public UserRMI getByEmail(String email) throws RemoteException {
        //Senseless

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public List<UserRMI> findAll() throws RemoteException {
        //Senseless

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public List<UserRMI> findByItemId(Long clientFileId) throws RemoteException {
        //Senseless

        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public void add(UserRMI usr) throws RemoteException {
        //Maybe it should be named create

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, usr.getId().toString(), false, null, false);
        user.setUser(usr);

        factory.disposeInstanceOf(UserRMI.class, usr.getId().toString(), true);

    }

    @Override
    public void update(UserRMI usr) throws RemoteException {
        //add and update are the same

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, usr.getId().toString(), false, null, false);
        user.setUser(usr);
        factory.disposeInstanceOf(UserRMI.class, usr.getId().toString(), true);

    }

    @Override
    public void deleteUser(UUID id) throws RemoteException {

        user = (UserRMI) factory.getInstanceOf(UserRMI.class, id.toString(), false, null, false);

        List<UUID> list = user.getWorkspaces();

        for (UUID w : list) {
            workspace = (WorkspaceRMI) factory.getInstanceOf(WorkspaceRMI.class, w.toString(), false, null, false);
            workspace.removeUser(id);
            factory.disposeInstanceOf(WorkspaceRMI.class, w.toString(), true);
        }

        factory.disposeInstanceOf(UserRMI.class, id.toString(), false);

    }

    //************************************
    //************************************
    //************** DEVICE **************
    //************************************
    //************************************
    @Override
    public DeviceRMI get(UUID id) throws RemoteException {

        List<DeviceRMI> devices = user.getDevices();

        for (DeviceRMI device : devices) {
            if (device.getId().equals(id)) {
                return device;
            }
        }

        return null;

    }

    @Override
    public void add(DeviceRMI device) throws RemoteException {

        List<DeviceRMI> devices = user.getDevices();

        if (!devices.contains(device)) {
            user.addDevice(device);
        }

        factory.disposeInstanceOf(UserRMI.class, user.getId().toString(), true);

    }

    @Override
    public void update(DeviceRMI device) throws RemoteException {

        List<DeviceRMI> devices = user.getDevices();

        for (DeviceRMI currentDevice : devices) {
            if (currentDevice.getId().equals(device.getId())) {
                devices.remove(currentDevice);
                devices.add(device);
                break;
            }
        }

        user.setDevices(devices);

        factory.disposeInstanceOf(UserRMI.class, user.getId().toString(), true);

    }

    @Override
    public void deleteDevice(UUID id) throws RemoteException {

        List<DeviceRMI> devices = user.getDevices();

        for (DeviceRMI currentDevice : devices) {
            if (currentDevice.getId().equals(id)) {
                devices.remove(currentDevice);
                break;
            }
        }

        user.setDevices(devices);

        factory.disposeInstanceOf(UserRMI.class, user.getId().toString(), true);

    }
}