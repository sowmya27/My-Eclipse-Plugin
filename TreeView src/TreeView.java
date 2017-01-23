package treeviewer;

import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;




public class TreeView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "treeviewer.TreeView";

	private TreeViewer viewer;
	private ViewContentProvider viewContentProvider;
	private Action doubleClickAction;
	
	public TreeNode GetTreeRoot()
	{
		return this.viewContentProvider.GetTreeRoot();
	}
	public void SetTreeRoot(TreeNode root)
	{
		this.viewContentProvider.SetTreeRoot(root);
	}
	class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;
		
		public TreeObject(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void SetName(String name){
			this.name = name;
		}
		public void setParent(TreeParent parent) {
			this.parent = parent;
		}
		public TreeParent getParent() {
			return parent;
		}
		public String toString() {
			return getName();
		}
		public Object getAdapter(Class key) {
			return null;
		}
	}
	
	class TreeParent extends TreeObject {
		private ArrayList children;
		public TreeParent(String name) {
			super(name);
			children = new ArrayList();
		}
		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}
		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}
		public TreeObject [] getChildren() {
			return (TreeObject [])children.toArray(new TreeObject[children.size()]);
		}
		public boolean hasChildren() {
			return children.size()>0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeParent invisibleRoot;
		private TreeNode treeRoot = null;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent)parent).hasChildren();
			return false;
		}
/*
 * We will set up a dummy model to initialize tree heararchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		private void initialize() {
			
			TreeParent root = GetTreeParent(treeRoot);
			
			invisibleRoot = new TreeParent("");
			invisibleRoot.addChild(root);
		}
		public TreeNode GetTreeRoot()
		{
			return GetTreeNode((TreeParent)invisibleRoot.children.get(0));
		}
		private TreeNode GetTreeNode(TreeParent current)
		{
			if(current.getName().equalsIgnoreCase("nil"))
			{
				return null;
			}
			TreeNode node = new TreeNode();
			node.data = current.getName();
			node.left = GetTreeNode((TreeParent)current.children.get(0));
			node.right = GetTreeNode((TreeParent)current.children.get(1));
			return node;
		}
		public void SetTreeRoot(TreeNode newRoot)
		{
			this.treeRoot = newRoot;
			this.initialize();
		}
		TreeParent GetTreeParent(TreeNode n)
		{
			if(n == null)
			{
				return new TreeParent("NIL");
			}
			TreeParent current = new TreeParent(n.data);
			TreeParent left = GetTreeParent(n.left);
			TreeParent right = GetTreeParent(n.right);
			current.addChild(left);
			current.addChild(right);
			return current;
		}
	}
	/**
	 * The constructor.
	 */
	public TreeView() {
	}
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.viewContentProvider = new ViewContentProvider();
		viewer.setContentProvider(this.viewContentProvider);
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "TreeViewer.viewer");
		makeActions();		
		hookDoubleClickAction();		
	}
	private void makeActions() {
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				TreeParent current = (TreeParent)((IStructuredSelection)selection).getFirstElement();
				InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), "", "Edit",current.getName(), null);
				if(dialog.open() == Window.OK)
				{
					String newName = dialog.getValue();
					if(current.getName() == "NIL")
					{
						TreeParent left = new TreeParent("NIL");
						TreeParent right = new TreeParent("NIL");
						current.addChild(left);
						current.addChild(right);
					}										
					if(newName.equalsIgnoreCase("NIL"))
					{
						current.children.remove(0);
						current.children.remove(0);
					}					
					current.SetName(newName);
					viewer.refresh();
					viewer.expandAll();
				}
			}
		};
	}
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
}